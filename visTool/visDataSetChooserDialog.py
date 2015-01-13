import sys, os

import visQt
QtCore = visQt.QtCore
QtGui = visQt.QtGui
import visContextAbstract

class DataSetChooserDialog(QtGui.QDialog):
    
    simulationSelected = QtCore.Signal(QtCore.QObject)

    def __init__(self, parent=None):
        super(DataSetChooserDialog, self).__init__(parent)
        self.setWindowTitle("Choose result set")
        self._dataSetComboBox = None
        self._selectedSim = None
        self.setAttribute(QtCore.Qt.WA_DeleteOnClose)

    def initUI(self, vis):
        self._vis = vis

        assert isinstance(self._vis,visContextAbstract.visContextAbstract)
        self.setObjectName("queryControlWidget")
        selfSizePolicy = QtGui.QSizePolicy(QtGui.QSizePolicy.Preferred, QtGui.QSizePolicy.Minimum)
        selfSizePolicy.setHorizontalStretch(0)
        selfSizePolicy.setVerticalStretch(0)
        selfSizePolicy.setHeightForWidth(self.sizePolicy().hasHeightForWidth())
        self.setSizePolicy(selfSizePolicy)
        self.setMinimumSize(300,150)
        gridLayout = QtGui.QGridLayout(self)
        gridLayout.setObjectName("gridLayout")

        self._dataSetComboBox = QtGui.QComboBox(self)

        gridLayout.addWidget(self._dataSetComboBox,0,0,)  
        openButton = QtGui.QPushButton("Open",self)
        cancelButton = QtGui.QPushButton("Cancel",self)
        buttonLayout = QtGui.QHBoxLayout()
        buttonLayout.addWidget(openButton)
        buttonLayout.addStretch(1)
        buttonLayout.addWidget(cancelButton)
        openButton.pressed.connect(self._openButtonPressedAction)
        cancelButton.pressed.connect(self._cancelButtonPressedAction)
        gridLayout.addLayout(buttonLayout,1,0)

        # Get available simulation dataset of open models from the VCell client
        simList = None
        try:
            print("calling self._vis.getVCellProxy().getSimsFromOpenModels()")
            self._vis.getVCellProxy().open()
            simList = self._vis.getVCellProxy().getClient().getSimsFromOpenModels()
        except:
            simList = None
            print("Exception looking for open model datasets")
        finally:
            self._vis.getVCellProxy().close()

        if (simList==None or len(simList)==0):
            raise Exception("No simulations found")
            return

        # populate the QComboBox if we found datasets
        for sim in simList:
            self._dataSetComboBox.addItem(sim.simName,sim)


    def _openButtonPressedAction(self):
        currentIndex = self._dataSetComboBox.currentIndex()
        if (currentIndex == None):
            return(None)
        self._selectedSim = self._dataSetComboBox.itemData(currentIndex)
        self.simulationSelected.emit(self)

    def _cancelButtonPressedAction(self):
        self.close()

    def getSelectedSimulation(self):
        return self._selectedSim


