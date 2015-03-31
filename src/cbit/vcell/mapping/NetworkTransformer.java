package cbit.vcell.mapping;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vcell.model.rbm.RbmNetworkGenerator;
import org.vcell.util.BeanUtils;
import org.vcell.util.Pair;
import org.vcell.util.TokenMangler;
import org.vcell.util.UserCancelException;

import cbit.vcell.bionetgen.BNGOutputFileParser;
import cbit.vcell.bionetgen.BNGOutputSpec;
import cbit.vcell.bionetgen.BNGParameter;
import cbit.vcell.bionetgen.BNGReaction;
import cbit.vcell.bionetgen.BNGSpecies;
import cbit.vcell.bionetgen.ObservableGroup;
import cbit.vcell.mapping.SimulationContext.MathMappingCallback;
import cbit.vcell.mapping.SimulationContext.NetworkGenerationRequirements;
import cbit.vcell.model.Kinetics.KineticsParameter;
import cbit.vcell.model.MassActionKinetics;
import cbit.vcell.model.Model;
import cbit.vcell.model.ModelException;
import cbit.vcell.model.Parameter;
import cbit.vcell.model.Product;
import cbit.vcell.model.RbmObservable;
import cbit.vcell.model.Reactant;
import cbit.vcell.model.ReactionStep;
import cbit.vcell.model.SimpleReaction;
import cbit.vcell.model.Species;
import cbit.vcell.model.SpeciesContext;
import cbit.vcell.parser.Expression;
import cbit.vcell.parser.ExpressionBindingException;
import cbit.vcell.parser.ExpressionException;
import cbit.vcell.parser.NameScope;
import cbit.vcell.parser.SymbolTableEntry;
import cbit.vcell.server.bionetgen.BNGInput;
import cbit.vcell.server.bionetgen.BNGOutput;
import cbit.vcell.server.bionetgen.BNGExecutorService;
import cbit.vcell.units.VCUnitDefinition;

/*
 * Flattening a Rule-based Model
 */
public class NetworkTransformer implements SimContextTransformer {

	private Map<String, Pair<SpeciesContext, Expression>> speciesEquivalenceMap = new HashMap<String, Pair<SpeciesContext, Expression>>();
		
	@Override
	final public SimContextTransformation transform(SimulationContext originalSimContext, MathMappingCallback mathMappingCallback, NetworkGenerationRequirements networkGenerationRequirements) {
		SimulationContext transformedSimContext;
		try {
			transformedSimContext = (SimulationContext)BeanUtils.cloneSerializable(originalSimContext);
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("unexpected exception: "+e.getMessage());
		}
		transformedSimContext.getModel().refreshDependencies();
		transformedSimContext.refreshDependencies1(false);

		ArrayList<ModelEntityMapping> entityMappings = new ArrayList<ModelEntityMapping>();
		
		transform(originalSimContext,transformedSimContext,entityMappings,mathMappingCallback,networkGenerationRequirements);
		
		ModelEntityMapping[] modelEntityMappings = entityMappings.toArray(new ModelEntityMapping[0]);
		
		return new SimContextTransformation(originalSimContext, transformedSimContext, modelEntityMappings);
	}
	
	public static class GeneratedSpeciesSymbolTableEntry implements SymbolTableEntry {
		private SymbolTableEntry unmappedSymbol = null;
		
		private GeneratedSpeciesSymbolTableEntry(SymbolTableEntry unmappedSymbol){
			this.unmappedSymbol = unmappedSymbol;
		}
		public boolean isConstant(){
			return false;
		}
		public String getName(){
			return unmappedSymbol.getName();
		}
		public NameScope getNameScope(){
			return null; // unmappedSymbol.getNameScope();
		}
		public VCUnitDefinition getUnitDefinition() {
			return unmappedSymbol.getUnitDefinition();
		}
		public Expression getExpression(){
			return null;
		}
		public double getConstantValue() throws ExpressionException {
			throw new ExpressionException("can't evaluate to constant");
		}
		@Override
		public int getIndex() {
			return 0;
		}
	};
	
	public String convertToBngl(SimulationContext simulationContext, boolean ignoreFunctions, MathMappingCallback mathMappingCallback, NetworkGenerationRequirements networkGenerationRequirements) {
		if (mathMappingCallback.isInterrupted()){
			throw new UserCancelException("canceled by user");
		}
		StringWriter bnglStringWriter = new StringWriter();
		PrintWriter pw = new PrintWriter(bnglStringWriter);
		RbmNetworkGenerator.writeBnglSpecial(simulationContext, pw, ignoreFunctions, speciesEquivalenceMap, networkGenerationRequirements);
		String bngl = bnglStringWriter.toString();
		pw.close();
		System.out.println(bngl);
//		for (Map.Entry<String, Pair<SpeciesContext, Expression>> entry : speciesEquivalenceMap.entrySet()) {
//	    String key = entry.getKey();
//	    Pair<SpeciesContext, Expression> value = entry.getValue();
//	    SpeciesContext sc = value.one;
//	    Expression initial = value.two;
//		System.out.println("key: " + key + ",   species: " + sc.getName() + ", initial: " + initial.infix());
//	}
		return bngl;
	}
	
	private BNGOutputSpec generateNetwork(SimulationContext simContext, MathMappingCallback mathMappingCallback, NetworkGenerationRequirements networkGenerationRequirements) {
		BNGOutputSpec outputSpec;
		String input = convertToBngl(simContext, true, mathMappingCallback, networkGenerationRequirements);
		for (Map.Entry<String, Pair<SpeciesContext, Expression>> entry : speciesEquivalenceMap.entrySet()) {
		    String key = entry.getKey();
		    Pair<SpeciesContext, Expression> value = entry.getValue();
		    SpeciesContext sc = value.one;
		    Expression initial = value.two;
			System.out.println("key: " + key + ",   species: " + sc.getName() + ", initial: " + initial.infix());
		}
		BNGInput bngInput = new BNGInput(input);
		BNGOutput bngOutput = null;
		try {
			final BNGExecutorService bngService = new BNGExecutorService(bngInput);
			bngOutput = bngService.executeBNG();
		} catch (RuntimeException ex) {
			ex.printStackTrace(System.out);
			throw ex; //rethrow without losing context
		} catch (Exception ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex.getMessage());
		}
		String bngNetString = bngOutput.getNetFileContent();
		outputSpec = BNGOutputFileParser.createBngOutputSpec(bngNetString);
//		BNGOutputFileParser.printBNGNetOutput(outputSpec);			// prints all output to console
		
		String message = "\nPlease go to the Specifications / Network panel and reduce the number of Iterations.";
		if(outputSpec.getBNGSpecies().length > 800) {
			message = "Species limit exceeded: max allowed number: 800, actual number: " + outputSpec.getBNGSpecies().length + message;
			throw new RuntimeException(message);
		}
		if(outputSpec.getBNGReactions().length > 2000) {
			message = "Reaction limit exceeded: max allowed number: 2000, actual number: " + outputSpec.getBNGReactions().length + message;
			throw new RuntimeException(message);
		}
		return outputSpec;
	}

	public void transform(SimulationContext simContext, SimulationContext transformedSimulationContext, ArrayList<ModelEntityMapping> entityMappings, MathMappingCallback mathMappingCallback, NetworkGenerationRequirements networkGenerationRequirements){

		mathMappingCallback.setMessage("starting network generation");
		
		long startTime = System.currentTimeMillis();
		System.out.println("Convert to bngl, execute BNG, retrieve the results.");
		BNGOutputSpec outputSpec = generateNetwork(simContext, mathMappingCallback, networkGenerationRequirements);
		long endTime = System.currentTimeMillis();
		long elapsedTime = endTime - startTime;
		System.out.println("     " + elapsedTime + " milliseconds");
		
		Model model = transformedSimulationContext.getModel();
		ReactionContext reactionContext = transformedSimulationContext.getReactionContext();
		try {
		startTime = System.currentTimeMillis();
		System.out.println("\nParameters :");
		for (int i = 0; i < outputSpec.getBNGParams().length; i++){
			BNGParameter p = outputSpec.getBNGParams()[i];
//			System.out.println(i+1 + ":\t\t"+ p.toString());
			if(model.getRbmModelContainer().getParameter(p.getName()) != null) {
//				System.out.println("   ...already exists.");
				continue;		// if it's already there we don't try to add it again; this should be true for all of them!
			}
			String s = p.getName();
			if(speciesEquivalenceMap.containsKey(s)) {
				continue;	// we get rid of the fake parameters we use as keys
			}
			Expression exp = new Expression(p.getValue());
			exp.bindExpression(model.getRbmModelContainer().getSymbolTable());
			model.getRbmModelContainer().addParameter(p.getName(), exp);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		System.out.println("     " + elapsedTime + " milliseconds");
		
		// ---- Species ------------------------------------------------------------------------------------------------------------
		startTime = System.currentTimeMillis();
		System.out.println("\nSpecies :");
		HashMap<Integer, String>  speciesMap = new HashMap<Integer, String>(); // the reactions will need this map to recover the names of species knowing only the networkFileIndex
		Map<String, Species> sMap = new HashMap<String, Species>();
		Map<String, SpeciesContext> scMap = new HashMap<String, SpeciesContext>();
		Map<String, BNGSpecies> crossMap = new HashMap<String, BNGSpecies>();
		List<SpeciesContext> noMapForThese = new ArrayList<SpeciesContext>();
		
		int countGenerated = 0;
		for (int i = 0; i < outputSpec.getBNGSpecies().length; i++){
			BNGSpecies s = outputSpec.getBNGSpecies()[i];
//			System.out.println(i+1 + ":\t\t"+ s.toString());
			
			String key = s.getConcentration().infix();
			if(key.startsWith(RbmNetworkGenerator.uniqueIdRoot)) {
			    Pair<SpeciesContext, Expression> value = speciesEquivalenceMap.get(key);
			    SpeciesContext sc = value.one;
			    Expression initial = value.two;
				s.setConcentration(initial);		// replace the fake initial condition with the real one
				
//				System.out.println(sc.getName() + ", " + sc.getSpecies().getCommonName() + "   ...is one of the original seed species.");
				speciesMap.put(s.getNetworkFileIndex(), sc.getName());		// existing name
				sMap.put(sc.getName(), sc.getSpecies());
				scMap.put(sc.getName(), sc);
				crossMap.put(sc.getName(), s);
				noMapForThese.add(sc);
				continue;
			}
			
			// all these species are new!
			int count = 0;				// generate unique name for the species
			String speciesName = null;
			String nameRoot = "s";
			
			if(s.getName() != null) {	// for seed species we generate a name from the species pattern
				nameRoot = s.getName();
				nameRoot = nameRoot.replaceAll("[!?~]+", "");
				nameRoot = TokenMangler.fixTokenStrict(nameRoot);
				while(true) {
					if(nameRoot.endsWith("_")) {		// clean all the '_' at the end, if any
						nameRoot = nameRoot.substring(0, nameRoot.length()-1);
					} else {
						break;
					}
				}
				if(model.getSpecies(nameRoot) == null && model.getSpeciesContext(nameRoot) == null && !sMap.containsKey(nameRoot) && !scMap.containsKey(nameRoot)) {
					speciesName = nameRoot;		// the name is good and unused
				} else {
					nameRoot += "_";
					while (true) {
						speciesName = nameRoot + count;	
						if (model.getSpecies(speciesName) == null && model.getSpeciesContext(speciesName) == null && !sMap.containsKey(speciesName) && !scMap.containsKey(speciesName)) {
							break;
						}	
						count++;
					}
				}
			} else {			// for plain species it works as before
				while (true) {
					speciesName = nameRoot + count;	
					if (model.getSpecies(speciesName) == null && model.getSpeciesContext(speciesName) == null && !sMap.containsKey(speciesName) && !scMap.containsKey(speciesName)) {
						break;
					}	
					count++;
				}
			}
			speciesMap.put(s.getNetworkFileIndex(), speciesName);				// newly created name
			SpeciesContext speciesContext = new SpeciesContext(new Species(speciesName, s.getName()), model.getStructure(0), null);
			speciesContext.setName(speciesName);
			model.addSpecies(speciesContext.getSpecies());
			model.addSpeciesContext(speciesContext);
			sMap.put(speciesName, speciesContext.getSpecies());
			scMap.put(speciesName, speciesContext);
			crossMap.put(speciesName, s);
			SpeciesContextSpec scs = reactionContext.getSpeciesContextSpec(speciesContext);
			Parameter param = scs.getParameter(SpeciesContextSpec.ROLE_InitialConcentration);
			param.setExpression(s.getConcentration());
			SpeciesContext origSpeciesContext = simContext.getModel().getSpeciesContext(s.getName());
			
			if (origSpeciesContext!=null){
				// TODO: execution never goes through here because we do a "continue" early in the for look
				// when we find one of the original seed species
				ModelEntityMapping em = new ModelEntityMapping(origSpeciesContext,speciesContext);
				entityMappings.add(em);
			}else{
				ModelEntityMapping em = new ModelEntityMapping(new GeneratedSpeciesSymbolTableEntry(speciesContext),speciesContext);
				entityMappings.add(em);
				countGenerated++;
			}
			if(i%50 == 0) {
				System.out.println(i+"");
			}
		}
		System.out.println("Total generated species: " + countGenerated);
		
//		System.out.println("------------------------ " + scMap.size() + " species contexts in the map.");
//		System.out.println("------------------------ " + model.getSpeciesContexts().length + " species contexts in the Model.");
//
//		SpeciesContext[] sca = new SpeciesContext[scMap.size()];
//		scMap.values().toArray(sca);
//		for(SpeciesContext sc1 : model.getSpeciesContexts()) {
//			boolean found = false;
//			for(SpeciesContext sc2 : sca) {
//				if(sc1 == sc2) {
//					found = true;
//					System.out.println("found species context " + sc1.getName() + " of species " + sc1.getSpecies().getCommonName() + " // " + sc2.getSpecies().getCommonName());
//					break;
//				}
//			}
//			if(found == false) {
//				System.out.println("species context not found " + sc1.getName());
//				scMap.put(sc1.getName(), sc1);
//				sMap.put(sc1.getName(), sc1.getSpecies());
//				noMapForThese.add(sc1);
//			}
//		}
//		Species[] sa = new Species[sMap.size()];
//		sMap.values().toArray(sa); 
//		for(Species s1 : model.getSpecies()) {
//			boolean found = false;
//			for(Species s2 : sa) {
//				if(s1 == s2) {
//					found = true;
//					System.out.println("found species " + s1.getCommonName());
//					break;
//				}
//			}
//			if(found == false) {
//				System.out.println("species not found " + s1.getCommonName());
//				
//			}
//		}
//
//		model.setSpecies(sa);
//		model.setSpeciesContexts(sca);
//		
//		for(SpeciesContext sc : sca) {
//			if(noMapForThese.contains(sc) {
//				continue;
//			}
//			SpeciesContextSpec scs = reactionContext.getSpeciesContextSpec(sc);
//			Parameter param = scs.getParameter(SpeciesContextSpec.ROLE_InitialConcentration);
//			BNGSpecies s = crossMap.get(sc.getName());
//			param.setExpression(s.getConcentration());
//			SpeciesContext origSpeciesContext = simContext.getModel().getSpeciesContext(s.getName());
//			if (origSpeciesContext!=null){
//				ModelEntityMapping em = new ModelEntityMapping(origSpeciesContext,sc);
//				entityMappings.add(em);
//			}else{
//				ModelEntityMapping em = new ModelEntityMapping(new GeneratedSpeciesSymbolTableEntry(sc),sc);
//				entityMappings.add(em);
//			}
//		}
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		System.out.println("     " + elapsedTime + " milliseconds");
		
		startTime = System.currentTimeMillis();
		System.out.println("\nReactions :");
		Map<String, ReactionStep> reactionStepMap = new HashMap<String, ReactionStep>();
		for (int i = 0; i < outputSpec.getBNGReactions().length; i++){
			BNGReaction r = outputSpec.getBNGReactions()[i];
//			System.out.println(i+1 + ":\t\t"+ r.writeReaction());
			int count=0;
			String reactionName = null;
			while (true) {
				reactionName = "r" + count;	
				if (model.getReactionStep(reactionName) == null && model.getRbmModelContainer().getReactionRule(reactionName) == null && !reactionStepMap.containsKey(reactionName)) {
					break;
				}	
				count++;
			}
			SimpleReaction sr = new SimpleReaction(model, model.getStructure(0), reactionName);
			for (int j = 0; j < r.getReactants().length; j++){
				BNGSpecies s = r.getReactants()[j];
				String scName = speciesMap.get(s.getNetworkFileIndex());
				SpeciesContext sc = model.getSpeciesContext(scName);
				Reactant reactant = sr.getReactant(scName);
				if(reactant == null) { 
					int stoichiometry = 1;
					sr.addReactant(sc, stoichiometry);
				} else {
					int stoichiometry = reactant.getStoichiometry();
					stoichiometry += 1;
					reactant.setStoichiometry(stoichiometry);
				}
			}
			for (int j = 0; j < r.getProducts().length; j++){
				BNGSpecies s = r.getProducts()[j];
				String scName = speciesMap.get(s.getNetworkFileIndex());
				SpeciesContext sc = model.getSpeciesContext(scName);
				Product product = sr.getProduct(scName);
				if(product == null) { 
					int stoichiometry = 1;
					sr.addProduct(sc, stoichiometry);
				} else {
					int stoichiometry = product.getStoichiometry();
					stoichiometry += 1;
					product.setStoichiometry(stoichiometry);
				}
			}
			MassActionKinetics k = new MassActionKinetics(sr);
			sr.setKinetics(k);
			KineticsParameter kforward = k.getForwardRateParameter();
			sr.getKinetics().setParameterValue(kforward, r.getParamExpression());
//			model.addReactionStep(sr);
			reactionStepMap.put(reactionName, sr);
		}
		ReactionStep[] reactionSteps = new ReactionStep[reactionStepMap.size()];
		reactionStepMap.values().toArray(reactionSteps); 
		model.setReactionSteps(reactionSteps);
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		System.out.println("     " + elapsedTime + " milliseconds");

		startTime = System.currentTimeMillis();
		System.out.println("\nObservables :");
		for (int i = 0; i < outputSpec.getObservableGroups().length; i++){
			ObservableGroup o = outputSpec.getObservableGroups()[i];
//			System.out.println(i+1 + ":\t\t" + o.toString());
			
			if(model.getRbmModelContainer().getParameter(o.getObservableGroupName()) != null) {
				System.out.println("   ...already exists.");
				continue;		// if it's already there we don't try to add it again; this should be true for all of them!
			}
			Expression exp = null;
			for (int j=0; j<o.getListofSpecies().length; j++){
				Expression term = Expression.mult(new Expression(o.getSpeciesMultiplicity()[j]),new Expression(speciesMap.get(o.getListofSpecies()[j].getNetworkFileIndex())));
				if (exp == null){
					exp = term;
				}else{
					exp = Expression.add(exp,term);
				}
			}
			exp.bindExpression(model.getRbmModelContainer().getSymbolTable());
			model.getRbmModelContainer().removeObservable(model.getRbmModelContainer().getObservable(o.getObservableGroupName()));
			Parameter newParameter = model.getRbmModelContainer().addParameter(o.getObservableGroupName(), exp);

			RbmObservable origObservable = simContext.getModel().getRbmModelContainer().getObservable(o.getObservableGroupName());
			ModelEntityMapping em = new ModelEntityMapping(origObservable,newParameter);
			entityMappings.add(em);
		}
		endTime = System.currentTimeMillis();
		elapsedTime = endTime - startTime;
		System.out.println("     " + elapsedTime + " milliseconds");

		} catch (PropertyVetoException ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex.getMessage());
		} catch (ExpressionBindingException ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex.getMessage());
		} catch (ModelException ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex.getMessage());
		} catch (ExpressionException ex) {
			ex.printStackTrace(System.out);
			throw new RuntimeException(ex.getMessage());
		}
		System.out.println("Done transforming");
		mathMappingCallback.setProgressFraction(1.0f/3.0f);
		mathMappingCallback.setMessage("done with network generation");
	}

}
