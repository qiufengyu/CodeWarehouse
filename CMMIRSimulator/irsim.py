# -*- coding: utf-8 -*-

import sys
import os
import platform
from PyQt5.QtCore import *
from PyQt5.QtGui import *
from PyQt5.QtWidgets import *
import ui
import resources_rc

__version__ = "1.1.0"

# Register some error class


class IRSyntaxError(Exception):
  pass


class DuplicatedLabelError(Exception):
  pass


class UndefinedLabelError(Exception):
  pass


class DuplicatedVariableError(Exception):
  pass


class CurrentFunctionNoneError(Exception):
  pass


class IRSim(QMainWindow, ui.Ui_MainWindow):
  def __init__(self, parent=None):
    super(IRSim, self).__init__(parent)

    # Some brushes
    self.cyanBrush = QBrush(Qt.cyan)
    self.yellowBrush = QBrush(Qt.yellow)
    self.noBrush = QBrush()

    # Read the UI design from Qt Designer file
    self.setupUi(self)
    self.rowLabel = QLabel()
    self.rowLabel.setFrameStyle(QFrame.StyledPanel|QFrame.Sunken)
    self.statusBar().addPermanentWidget(self.rowLabel)

    # Initialize some values
    # self.initialize()
    self.fileName = None
    self.ip = -1
    self.entranceIP = -1
    self.pauseRunning = False
    self.offset = 0
    self.instructionCount = 0
    # codeList
    self.codes = list()
    # memory space for the program
    self.mem = None
    self.functionDict = dict()
    self.currentFunction = None
    self.symbolTable = dict()
    self.labelTable = dict()
    self.callStack = list()
    self.argumentStack = list()
    self.codeList.clear()
    self.tableWidget.setRowCount(0)
    self.tableWidget.clearContents()
    self.textEdit.clear()
    self.updateStatus(None)
    self.lineNo = 0

    # Connect the signals with desired slots
    self.action_Open.triggered.connect(self.fileOpen)
    self.action_Quit.triggered.connect(self.close)
    self.action_Info.triggered.connect(self.info)
    self.action_Run.triggered.connect(self.run)
    self.action_Step.triggered.connect(self.step)
    self.action_Stop.triggered.connect(self.stop)
    self.codeList.currentRowChanged.connect(self.updateRowLabel)

  def initialize(self):
    self.fileName = None
    self.ip = -1
    self.entranceIP = -1
    self.pauseRunning = False
    self.offset = 0
    self.instructionCount = 0
    # codeList
    self.codes = list()
    # memory space for the program
    self.mem = None
    self.functionDict = dict()
    self.currentFunction = None
    self.symbolTable = dict()
    self.labelTable = dict()
    self.callStack = list()
    self.argumentStack = list()
    self.codeList.clear()
    self.tableWidget.setRowCount(0)
    self.tableWidget.clearContents()
    self.textEdit.clear()
    self.updateStatus(None)
    self.lineNo = 0

  # Open an existing IR file
  def fileOpen(self):
    if self.okToContinue():
      fDir = '.' if self.fileName is None else os.path.dirname(self.fileName)
      fName = QFileDialog.getOpenFileName(self,
        "IR Simulator - Open IR file",
        fDir,
        "IR files (*ir)")[0]
      if fName:
        self.initialize()
        self.loadFile(fName)

  # Load the contents of the file
  def loadFile(self, fName):
    lineNo = 0
    with open(fName, 'r', encoding='utf-8') as f:
      for line in f:
        if line.isspace():
          continue
        if self.sanityCheck(line, lineNo):
          self.codeList.addItem(line.strip().replace('\t', ' '))
        else:
          break
        lineNo += 1
      else:
        self.fileName = fName
        self.lineNo = lineNo
    if self.entranceIP == -1:
      QMessageBox.critical(self, 'Error',
        'Cannot find program entrance, please make sure the <b>"main"</b> function does exitst')
    # to do check
    if self.fileName is None or (not self.labelCheck()) or (self.offset > 0x100000) or (self.entranceIP == -1):
      self.updateStatus('Loading file failed.')
      self.initialize()
      return

    self.mem = [0]*262144
    self.displayWatchTable()
    self.updateStatus('File loaded successfully.')

  def run(self):
    self.stop()
    self.ip = self.entranceIP
    while True:
      if self.ip < 0 or self.ip >= len(self.codes):
        error_code = 3
        break
      code = self.codes[self.ip]
      # error_code: [0 = nextIR; 1 = finish; 2 = memAccessError; 3 = IPError
      error_code = self.execute_code(code)
      # print(error_code)
      if error_code > 0:
        break
      self.ip += 1
    if error_code == 1:
      QMessageBox.information(self, 'Finish',
                              'Program has exited gracefully.\nTotal instructions = %d' % self.instructionCount)
      self.updateStatus('Simulation OK. Instruction count = %d' % self.instructionCount)
    elif error_code == 2:
      QMessageBox.critical(self, 'Error',
                           'An error occurred at line %d: Illegal memory access. \nIf this message keeps popping out, please reload the source file' % (
                           self.ip + 1))
      self.updateStatus('Simulation failed: Memory access out of bound.')
    elif error_code == 3:
      QMessageBox.warning(self, 'Warning',
                          'Program Counter goes out of bound. The running program will be terminated instantly.')
      self.updateStatus('Simulation failed: PC error.')
    self.tableWidget.setRowCount(0)
    self.displayWatchTable()
    self.ip = -1

  def stop(self):
    if self.ip != -1:
      self.codeList.item(self.ip).setBackground(self.noBrush)
    self.ip = -1
    self.instructionCount = 0
    self.pauseRunning = False
    self.tableWidget.setRowCount(0)
    self.mem = [0] * 262144
    self.callStack = list()
    self.argumentStack = list()
    self.displayWatchTable()
    self.codeList.setCurrentRow(-1)
    self.textEdit.clear()
    # self.updateStatus("Stopped")

  def step(self):
    if self.ip == -1:
      self.stop()
      self.pauseRunning = True
      self.ip = self.entranceIP - 1
    else:
      self.codeList.item(self.ip).setBackground(self.noBrush)
    self.ip += 1
    if self.ip < 0 or self.ip >= len(self.codes):
      QMessageBox.warning(self, 'Warning',
                          'Program Counter goes out of bound. The running program will be terminated instantly.')
      self.updateStatus('Simulation failed: PC error.')
      self.ip = -1
      self.pauseRunning = False
      return
    code = self.codes[self.ip]
    error_code = self.execute_code(code)
    if error_code == 1:
      QMessageBox.information(self, 'Finish', 'Program has exited gracefully.\nTotal instructions = %d' % self.instructionCount)
      self.updateStatus('Simulation OK. Instruction count = %d' % self.instructionCount)
      self.ip = -1
      self.pauseRunning = False
    elif error_code == 2:
      QMessageBox.critical(self, 'Error', 'An error occurred at line %d: Illegal memory access' % (self.ip + 1))
      self.updateStatus('Simulation failed: Memory access out of bound')
      self.ip = -1
      self.pauseRunning = False
    else:
      self.codeList.item(self.ip).setBackground(self.cyanBrush)
      self.codeList.setCurrentRow(self.ip)
    self.tableWidget.setRowCount(0)
    self.displayWatchTable()

  def execute_code(self, code):
    # print("Executing code: ", code)
    self.instructionCount += 1
    try:
      if code[0] == 'READ':
        readValue, ok = QInputDialog.getInt(self, 'IR Simulator - Read', 'Please enter an integral value for %s' % code[1], 0)
        if ok:
          self.mem[self.symbolTable[code[1]][0] // 4] = readValue
      elif code[0] == 'WRITE':
        self.textEdit.append(str(self.getValue(code[1])))
      elif code[0] == 'GOTO':
        self.ip = self.labelTable[code[1]]
      elif code[0] == 'IF':
        value1 = self.getValue(code[1])
        value2 = self.getValue(code[3])
        if eval(str(value1)+code[2]+str(value2)):
          self.ip = self.labelTable[code[4]]
      elif code[0] == 'MOV':
        value = self.getValue(code[2])
        if code[1][0] == '*':
          self.mem[self.mem[self.symbolTable[code[1][1:]][0] // 4] // 4] = value
        else:
          self.mem[self.symbolTable[code[1]][0] // 4] = value
      elif code[0] == 'ARITH':
        value1 = self.getValue(code[2])
        value2 = self.getValue(code[4])
        if code[3] == '/':
          self.mem[self.symbolTable[code[1]][0] // 4] = eval(str(value1) + "//" + str(value2))
        else:
          self.mem[self.symbolTable[code[1]][0] // 4] = eval(str(value1)+ code[3] + str(value2))
      elif code[0] == 'RETURN':
        if len(self.callStack) == 0:
          return 1
        else:
          returnValue = self.getValue(code[1])
          stackItem = self.callStack.pop()
          self.ip = stackItem[0]
          for key in stackItem[2].keys():
            self.symbolTable[key] = stackItem[2][key]
          self.offset = stackItem[3]
          self.mem[self.symbolTable[stackItem[1]][0] // 4] = returnValue
      elif code[0] == 'CALL':
        oldAddrs = dict()
        oldOffset = self.offset
        for key in self.functionDict[code[2]]:
          oldAddrs[key] = self.symbolTable[key]
          self.symbolTable[key] = self.getNewAddr(self.symbolTable[key][1]), self.symbolTable[key][1], self.symbolTable[key][2]
        self.callStack.append((self.ip, code[1], oldAddrs, oldOffset))
        self.ip = self.labelTable[code[2]]
      elif code[0] == 'ARG':
        self.argumentStack.append(self.getValue(code[1]))
      elif code[0] == 'PARAM':
        self.mem[self.symbolTable[code[1]][0] // 4] = self.argumentStack.pop()
    except IndexError:
      return 2
    return 0

  def getNewAddr(self, size):
    ret = self.offset
    self.offset = self.offset + size
    return ret

  def updateStatus(self, message):
    if message:
      self.statusBar().showMessage(message, 5000)
    if self.fileName is None:
      self.setWindowTitle('IR Simulator')
      self.action_Run.setEnabled(False)
      self.action_Step.setEnabled(False)
      self.action_Stop.setEnabled(False)
    else:
      self.setWindowTitle('IR Simulator - {0}'.format(os.path.basename(self.fileName)))
      self.action_Run.setEnabled(True)
      self.action_Step.setEnabled(True)
      self.action_Stop.setEnabled(True)

  def updateRowLabel(self):
    row = self.codeList.currentRow()
    if row >= 0:
      self.rowLabel.setText('Line %d' % (row+1))
    else:
      self.rowLabel.setText('')

  # When current file is running, warn the user when he wants to open a new file
  def okToContinue(self):
    if self.ip != -1:
      reply = QMessageBox.questin(self,
        'IR Simulator - Warning',
        'You are running a program currently. Stop running and proceed?',
        QMessageBox.Yes|QMessageBox.No)
      if reply == QMessageBox.No:
        return False
      self.ip = -1
    return True

  def sanityCheck(self, code, lineNo):
    tokens = code.split()
    replops = ['>', '<', '>=', '<=', '==', '!=']
    arithops = ['+', '-', '*', '/']
    try:
      if tokens[0] == 'LABEL' or tokens[0] == 'FUNCTION':
        if (len(tokens) != 3) or tokens[2] != ':':
          raise IRSyntaxError
        if tokens[1] in self.labelTable:
          raise DuplicatedLabelError
        self.labelTable[tokens[1]] = lineNo
        if tokens[1] == 'main':
          if tokens[0] == 'LABEL':
            raise IRSyntaxError
          self.entranceIP = lineNo
        if tokens[0] == 'FUNCTION':
          self.currentFunction = tokens[1]
          self.functionDict[tokens[1]] = list()
        self.codes.append(('LABEL', tokens[1]))
      else:
        if self.currentFunction is None:
          raise CurrentFunctionNoneError
        if tokens[0] == 'GOTO':
          if len(tokens) != 2:
            raise IRSyntaxError
          self.codes.append(('GOTO', tokens[1]))
        elif tokens[0] == 'RETURN' or tokens[0] =='READ' or tokens[0] == 'WRITE' or tokens[0] == 'ARG' or tokens[0] == 'PARAM':
          if len(tokens) != 2:
            raise IRSyntaxError
          if (tokens[0] == 'READ' or tokens[0] =='PARAM') and (not tokens[1][0].isalpha()):
            raise IRSyntaxError
          self.tableInsert(tokens[1])
          self.codes.append((tokens[0], tokens[1]))
        elif tokens[0] == 'DEC':
          if len(tokens) != 3 or (int(tokens[2]) % 4 != 0):
            raise IRSyntaxError
          if tokens[1] in self.symbolTable:
            raise DuplicatedVariableError
          self.tableInsert(tokens[1], int(tokens[2]), True)
          self.codes.append('DEC')
        elif tokens[0] == 'IF':
          if len(tokens) != 6 or tokens[4] != 'GOTO' or tokens[2] not in replops:
            raise IRSyntaxError
          self.tableInsert(tokens[1])
          self.tableInsert(tokens[3])
          self.codes.append(('IF', tokens[1], tokens[2], tokens[3], tokens[5]))
        else:
          if tokens[1] != ':=' or len(tokens) < 3:
            raise IRSyntaxError
          if tokens[0][0] == '&' or tokens[0][0] == '#':
            raise IRSyntaxError
          self.tableInsert(tokens[0])
          if tokens[2] == 'CALL':
            if len(tokens) != 4:
              raise IRSyntaxError
            self.codes.append(('CALL', tokens[0], tokens[3]))
          elif len(tokens) == 3:
            self.tableInsert(tokens[2])
            self.codes.append(('MOV', tokens[0], tokens[2]))
          elif len(tokens) == 5 and tokens[3] in arithops:
            self.tableInsert(tokens[2])
            self.tableInsert(tokens[4])
            self.codes.append(('ARITH', tokens[0], tokens[2], tokens[3], tokens[4]))
          else:
            raise IRSyntaxError
    except (IRSyntaxError, ValueError):
      QMessageBox.critical(self, 'Error', 'Syntax error at line %d:\n\n%s' % (lineNo+1, code))
      return False
    except DuplicatedLabelError:
      QMessageBox.critical(self, 'Error', 'Duplicated label %s at line %d:\n\n%s' % (tokens[1], lineNo+1, code))
      return False
    except DuplicatedVariableError:
      QMessageBox.critical(self, 'Error', 'Duplicated variable %s at line %d:\n\n%s' % (tokens[1], lineNo+1, code))
      return False
    except CurrentFunctionNoneError:
      QMessageBox.critical(self, 'Error', 'Line %d does not belong to any function:\n\n%s' % (lineNo+1, code))
      return False
    return True

  def labelCheck(self):
    try:
      for i in range(self.lineNo):
        code = self.codeList.item(i).text();
        tokens = code.split()
        if tokens[0] == 'GOTO':
          if tokens[1] not in self.labelTable:
            raise UndefinedLabelError
          elif tokens[0] == 'IF':
            if tokens[5] not in self.labelTable:
              raise UndefinedLabelError
          elif len(tokens) > 2 and tokens[2] == 'CALL':
            if tokens[3] not in self.labelTable:
              raise UndefinedLabelError
    except UndefinedLabelError:
      QMessageBox.critical(self, 'Error', 'Undefined label at line %d:\n\n%s' % (i + 1, code))
      return False
    return True

  # Insert variables into symbolTable
  def tableInsert(self, var, size=4, array=False):
    if var.isdigit():
      raise IRSyntaxError
    if var[0] == '&' or var[0] == '*':
      var = var[1:]
    elif var[0] == '#':
      test = int(var[1:])
      return
    if var in self.symbolTable:
      return
    self.functionDict[self.currentFunction].append(var)
    if self.currentFunction == 'main':
      self.symbolTable[var] = self.offset, size, array
      self.offset += size
    else:
      self.symbolTable[var] = -1, size, array

  # Get value from a variable
  def getValue(self, var):
    if var[0] == '#':
      return int(var[1:])
    elif var[0] == '&':
      return self.symbolTable[var[1:]][0]
    elif var[0] == '*':
      return self.mem[self.mem[self.symbolTable[var[1:]][0] // 4] // 4]
    else:
      return self.mem[self.symbolTable[var][0] // 4]

  # Demonstrate watchTable from symbolTable
  def displayWatchTable(self):
    # Set background color of horizontal headers, failed
    for row, key in enumerate(self.symbolTable.keys()):
      self.tableWidget.insertRow(row)
      item = QTableWidgetItem(key)
      item.setTextAlignment(Qt.AlignRight | Qt.AlignVCenter)
      self.tableWidget.setItem(row, 0, item)
      if self.symbolTable[key][0] >= 0:
        item = QTableWidgetItem(str(self.symbolTable[key][0]))
      else:
        item = QTableWidgetItem('N/A')
      item.setTextAlignment(Qt.AlignRight|Qt.AlignVCenter)
      self.tableWidget.setItem(row, 1, item)
      if self.symbolTable[key][0] < 0:
        item = QTableWidgetItem('N/A')
      elif self.symbolTable[key][1] > 4:
        strs = str(self.mem[(self.symbolTable[key][0] // 4) : (self.symbolTable[key][0] // 4 + self.symbolTable[key][1] // 4)])
        item = QTableWidgetItem(strs)
      else:
        item = QTableWidgetItem(str(self.mem[self.symbolTable[key][0] // 4]))
      item.setTextAlignment(Qt.AlignRight | Qt.AlignVCenter)
      self.tableWidget.setItem(row, 2, item)
    self.tableWidget.sortItems(0)

  def info(self):
    QMessageBox.about(self, "About IR Simulator",
      """<b>IR Simulator</b> v {0}
      <p>Copyright &copy; 2012 Grieve originally.
      <p>All rights reserved.
      <p>Revised by Godfray, 2017.
      <p>This application can be used to simulate the IR 
      designed for the Compiler course here at NJU.
      <p>Python {1} - Qt {2} - PyQt {3} on {4}""".format(
      __version__, platform.python_version(),
      QT_VERSION_STR, PYQT_VERSION_STR,
      platform.system()))


app = QApplication(sys.argv)
app.setOrganizationName('Nanjing University')
app.setApplicationName('IR Simulator')
app.setWindowIcon(QIcon(':/favicon.ico'))
app.setStyle('Plastique')
form = IRSim()
form.show()
app.exec_()
