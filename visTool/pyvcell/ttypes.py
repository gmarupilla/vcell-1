#
# Autogenerated by Thrift Compiler (0.9.2)
#
# DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
#
#  options string: py
#

from thrift.Thrift import TType, TMessageType, TException, TApplicationException

from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol, TProtocol
try:
  from thrift.protocol import fastbinary
except:
  fastbinary = None


class DomainType:
  VOLUME = 0
  MEMBRANE = 1

  _VALUES_TO_NAMES = {
    0: "VOLUME",
    1: "MEMBRANE",
  }

  _NAMES_TO_VALUES = {
    "VOLUME": 0,
    "MEMBRANE": 1,
  }


class VariableInfo:
  """
  Attributes:
   - variableVtuName
   - variableDisplayName
   - domainName
   - variableDomainType
   - unitsLabel
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'variableVtuName', None, None, ), # 1
    (2, TType.STRING, 'variableDisplayName', None, None, ), # 2
    (3, TType.STRING, 'domainName', None, None, ), # 3
    (4, TType.I32, 'variableDomainType', None, None, ), # 4
    (5, TType.STRING, 'unitsLabel', None, None, ), # 5
  )

  def __init__(self, variableVtuName=None, variableDisplayName=None, domainName=None, variableDomainType=None, unitsLabel=None,):
    self.variableVtuName = variableVtuName
    self.variableDisplayName = variableDisplayName
    self.domainName = domainName
    self.variableDomainType = variableDomainType
    self.unitsLabel = unitsLabel

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.variableVtuName = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRING:
          self.variableDisplayName = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.STRING:
          self.domainName = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 4:
        if ftype == TType.I32:
          self.variableDomainType = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 5:
        if ftype == TType.STRING:
          self.unitsLabel = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('VariableInfo')
    if self.variableVtuName is not None:
      oprot.writeFieldBegin('variableVtuName', TType.STRING, 1)
      oprot.writeString(self.variableVtuName)
      oprot.writeFieldEnd()
    if self.variableDisplayName is not None:
      oprot.writeFieldBegin('variableDisplayName', TType.STRING, 2)
      oprot.writeString(self.variableDisplayName)
      oprot.writeFieldEnd()
    if self.domainName is not None:
      oprot.writeFieldBegin('domainName', TType.STRING, 3)
      oprot.writeString(self.domainName)
      oprot.writeFieldEnd()
    if self.variableDomainType is not None:
      oprot.writeFieldBegin('variableDomainType', TType.I32, 4)
      oprot.writeI32(self.variableDomainType)
      oprot.writeFieldEnd()
    if self.unitsLabel is not None:
      oprot.writeFieldBegin('unitsLabel', TType.STRING, 5)
      oprot.writeString(self.unitsLabel)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.variableVtuName is None:
      raise TProtocol.TProtocolException(message='Required field variableVtuName is unset!')
    if self.variableDisplayName is None:
      raise TProtocol.TProtocolException(message='Required field variableDisplayName is unset!')
    if self.domainName is None:
      raise TProtocol.TProtocolException(message='Required field domainName is unset!')
    if self.variableDomainType is None:
      raise TProtocol.TProtocolException(message='Required field variableDomainType is unset!')
    if self.unitsLabel is None:
      raise TProtocol.TProtocolException(message='Required field unitsLabel is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.variableVtuName)
    value = (value * 31) ^ hash(self.variableDisplayName)
    value = (value * 31) ^ hash(self.domainName)
    value = (value * 31) ^ hash(self.variableDomainType)
    value = (value * 31) ^ hash(self.unitsLabel)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class SimulationDataSetRef:
  """
  Attributes:
   - simId
   - simName
   - modelId
   - username
   - userkey
   - jobIndex
   - isMathModel
   - simulationContextName
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'simId', None, None, ), # 1
    (2, TType.STRING, 'simName', None, None, ), # 2
    (3, TType.STRING, 'modelId', None, None, ), # 3
    (4, TType.STRING, 'username', None, None, ), # 4
    (5, TType.STRING, 'userkey', None, None, ), # 5
    (6, TType.I32, 'jobIndex', None, None, ), # 6
    (7, TType.BOOL, 'isMathModel', None, None, ), # 7
    (8, TType.STRING, 'simulationContextName', None, None, ), # 8
  )

  def __init__(self, simId=None, simName=None, modelId=None, username=None, userkey=None, jobIndex=None, isMathModel=None, simulationContextName=None,):
    self.simId = simId
    self.simName = simName
    self.modelId = modelId
    self.username = username
    self.userkey = userkey
    self.jobIndex = jobIndex
    self.isMathModel = isMathModel
    self.simulationContextName = simulationContextName

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.simId = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.STRING:
          self.simName = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 3:
        if ftype == TType.STRING:
          self.modelId = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 4:
        if ftype == TType.STRING:
          self.username = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 5:
        if ftype == TType.STRING:
          self.userkey = iprot.readString();
        else:
          iprot.skip(ftype)
      elif fid == 6:
        if ftype == TType.I32:
          self.jobIndex = iprot.readI32();
        else:
          iprot.skip(ftype)
      elif fid == 7:
        if ftype == TType.BOOL:
          self.isMathModel = iprot.readBool();
        else:
          iprot.skip(ftype)
      elif fid == 8:
        if ftype == TType.STRING:
          self.simulationContextName = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('SimulationDataSetRef')
    if self.simId is not None:
      oprot.writeFieldBegin('simId', TType.STRING, 1)
      oprot.writeString(self.simId)
      oprot.writeFieldEnd()
    if self.simName is not None:
      oprot.writeFieldBegin('simName', TType.STRING, 2)
      oprot.writeString(self.simName)
      oprot.writeFieldEnd()
    if self.modelId is not None:
      oprot.writeFieldBegin('modelId', TType.STRING, 3)
      oprot.writeString(self.modelId)
      oprot.writeFieldEnd()
    if self.username is not None:
      oprot.writeFieldBegin('username', TType.STRING, 4)
      oprot.writeString(self.username)
      oprot.writeFieldEnd()
    if self.userkey is not None:
      oprot.writeFieldBegin('userkey', TType.STRING, 5)
      oprot.writeString(self.userkey)
      oprot.writeFieldEnd()
    if self.jobIndex is not None:
      oprot.writeFieldBegin('jobIndex', TType.I32, 6)
      oprot.writeI32(self.jobIndex)
      oprot.writeFieldEnd()
    if self.isMathModel is not None:
      oprot.writeFieldBegin('isMathModel', TType.BOOL, 7)
      oprot.writeBool(self.isMathModel)
      oprot.writeFieldEnd()
    if self.simulationContextName is not None:
      oprot.writeFieldBegin('simulationContextName', TType.STRING, 8)
      oprot.writeString(self.simulationContextName)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.simId is None:
      raise TProtocol.TProtocolException(message='Required field simId is unset!')
    if self.simName is None:
      raise TProtocol.TProtocolException(message='Required field simName is unset!')
    if self.modelId is None:
      raise TProtocol.TProtocolException(message='Required field modelId is unset!')
    if self.username is None:
      raise TProtocol.TProtocolException(message='Required field username is unset!')
    if self.userkey is None:
      raise TProtocol.TProtocolException(message='Required field userkey is unset!')
    if self.jobIndex is None:
      raise TProtocol.TProtocolException(message='Required field jobIndex is unset!')
    if self.isMathModel is None:
      raise TProtocol.TProtocolException(message='Required field isMathModel is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.simId)
    value = (value * 31) ^ hash(self.simName)
    value = (value * 31) ^ hash(self.modelId)
    value = (value * 31) ^ hash(self.username)
    value = (value * 31) ^ hash(self.userkey)
    value = (value * 31) ^ hash(self.jobIndex)
    value = (value * 31) ^ hash(self.isMathModel)
    value = (value * 31) ^ hash(self.simulationContextName)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class PlotData:
  """
  Attributes:
   - timePoints
   - data
  """

  thrift_spec = (
    None, # 0
    (1, TType.LIST, 'timePoints', (TType.DOUBLE,None), None, ), # 1
    (2, TType.LIST, 'data', (TType.DOUBLE,None), None, ), # 2
  )

  def __init__(self, timePoints=None, data=None,):
    self.timePoints = timePoints
    self.data = data

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.LIST:
          self.timePoints = []
          (_etype3, _size0) = iprot.readListBegin()
          for _i4 in xrange(_size0):
            _elem5 = iprot.readDouble();
            self.timePoints.append(_elem5)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.LIST:
          self.data = []
          (_etype9, _size6) = iprot.readListBegin()
          for _i10 in xrange(_size6):
            _elem11 = iprot.readDouble();
            self.data.append(_elem11)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('PlotData')
    if self.timePoints is not None:
      oprot.writeFieldBegin('timePoints', TType.LIST, 1)
      oprot.writeListBegin(TType.DOUBLE, len(self.timePoints))
      for iter12 in self.timePoints:
        oprot.writeDouble(iter12)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    if self.data is not None:
      oprot.writeFieldBegin('data', TType.LIST, 2)
      oprot.writeListBegin(TType.DOUBLE, len(self.data))
      for iter13 in self.data:
        oprot.writeDouble(iter13)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.timePoints is None:
      raise TProtocol.TProtocolException(message='Required field timePoints is unset!')
    if self.data is None:
      raise TProtocol.TProtocolException(message='Required field data is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.timePoints)
    value = (value * 31) ^ hash(self.data)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class PostProcessingData:
  """
  Attributes:
   - variableList
   - plotData
  """

  thrift_spec = (
    None, # 0
    (1, TType.LIST, 'variableList', (TType.STRUCT,(VariableInfo, VariableInfo.thrift_spec)), None, ), # 1
    (2, TType.LIST, 'plotData', (TType.STRUCT,(PlotData, PlotData.thrift_spec)), None, ), # 2
  )

  def __init__(self, variableList=None, plotData=None,):
    self.variableList = variableList
    self.plotData = plotData

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.LIST:
          self.variableList = []
          (_etype17, _size14) = iprot.readListBegin()
          for _i18 in xrange(_size14):
            _elem19 = VariableInfo()
            _elem19.read(iprot)
            self.variableList.append(_elem19)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      elif fid == 2:
        if ftype == TType.LIST:
          self.plotData = []
          (_etype23, _size20) = iprot.readListBegin()
          for _i24 in xrange(_size20):
            _elem25 = PlotData()
            _elem25.read(iprot)
            self.plotData.append(_elem25)
          iprot.readListEnd()
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('PostProcessingData')
    if self.variableList is not None:
      oprot.writeFieldBegin('variableList', TType.LIST, 1)
      oprot.writeListBegin(TType.STRUCT, len(self.variableList))
      for iter26 in self.variableList:
        iter26.write(oprot)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    if self.plotData is not None:
      oprot.writeFieldBegin('plotData', TType.LIST, 2)
      oprot.writeListBegin(TType.STRUCT, len(self.plotData))
      for iter27 in self.plotData:
        iter27.write(oprot)
      oprot.writeListEnd()
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.variableList is None:
      raise TProtocol.TProtocolException(message='Required field variableList is unset!')
    if self.plotData is None:
      raise TProtocol.TProtocolException(message='Required field plotData is unset!')
    return


  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.variableList)
    value = (value * 31) ^ hash(self.plotData)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)

class ThriftDataAccessException(TException):
  """
  Attributes:
   - message
  """

  thrift_spec = (
    None, # 0
    (1, TType.STRING, 'message', None, None, ), # 1
  )

  def __init__(self, message=None,):
    self.message = message

  def read(self, iprot):
    if iprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and isinstance(iprot.trans, TTransport.CReadableTransport) and self.thrift_spec is not None and fastbinary is not None:
      fastbinary.decode_binary(self, iprot.trans, (self.__class__, self.thrift_spec))
      return
    iprot.readStructBegin()
    while True:
      (fname, ftype, fid) = iprot.readFieldBegin()
      if ftype == TType.STOP:
        break
      if fid == 1:
        if ftype == TType.STRING:
          self.message = iprot.readString();
        else:
          iprot.skip(ftype)
      else:
        iprot.skip(ftype)
      iprot.readFieldEnd()
    iprot.readStructEnd()

  def write(self, oprot):
    if oprot.__class__ == TBinaryProtocol.TBinaryProtocolAccelerated and self.thrift_spec is not None and fastbinary is not None:
      oprot.trans.write(fastbinary.encode_binary(self, (self.__class__, self.thrift_spec)))
      return
    oprot.writeStructBegin('ThriftDataAccessException')
    if self.message is not None:
      oprot.writeFieldBegin('message', TType.STRING, 1)
      oprot.writeString(self.message)
      oprot.writeFieldEnd()
    oprot.writeFieldStop()
    oprot.writeStructEnd()

  def validate(self):
    if self.message is None:
      raise TProtocol.TProtocolException(message='Required field message is unset!')
    return


  def __str__(self):
    return repr(self)

  def __hash__(self):
    value = 17
    value = (value * 31) ^ hash(self.message)
    return value

  def __repr__(self):
    L = ['%s=%r' % (key, value)
      for key, value in self.__dict__.iteritems()]
    return '%s(%s)' % (self.__class__.__name__, ', '.join(L))

  def __eq__(self, other):
    return isinstance(other, self.__class__) and self.__dict__ == other.__dict__

  def __ne__(self, other):
    return not (self == other)
