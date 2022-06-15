import java.awt.{Color, Component, Dimension}
import javax.swing._
import javax.swing.table.TableCellRenderer

object Example {

  def generateMultiplicationTable(min: Int, max: Int): List[List[Int]] = {
    var leftTerm = max
    var table: List[List[Int]] = Nil
    while (leftTerm >= min) {
      var rightTerm = max
      var row: List[Int] = Nil
      while (rightTerm >= min) {
        row = (leftTerm * rightTerm) :: row
        rightTerm -= 1
      }
      table = row :: table
      leftTerm -= 1
    }
    table
  }

  def generateJTable(min: Int, max: Int): JTable = {
    val mulTable: List[List[Int]] = generateMultiplicationTable(min, max)
    val intervalLs: List[Int] = (min to max).toList
    val jtable = new JTable(intervalLs.size + 1, intervalLs.size + 1)
    for (i <- intervalLs) {
      val rowCol = i - min + 1
      jtable.setValueAt(i, rowCol, 0)
      jtable.setValueAt(i, 0, rowCol)
    }
    var rowIdx = 0
    for (row: List[Int] <- mulTable) {
      var colIdx = 0
      for (entry <- row) {
        jtable.setValueAt(entry, rowIdx + 1, colIdx + 1)
        colIdx += 1
      }
      rowIdx += 1
    }
    jtable
  }

  ////// Uninteresting code for GUI //////////////////////////

  def main(args: Array[String]): Unit = {
    val width = 700
    val height = 350
    val frame = new JFrame("Multiplication table")
    val rootPane = new JPanel()
    frame.getContentPane.add(rootPane)
    val vBoxLayout = new BoxLayout(rootPane, BoxLayout.Y_AXIS)
    rootPane.setLayout(vBoxLayout)
    val commandBar = new JPanel()
    val hBoxLayout = new BoxLayout(commandBar, BoxLayout.X_AXIS)
    commandBar.setLayout(hBoxLayout)
    val minSpinner = new JSpinner()
    minSpinner.setValue(1)
    val maxSpinner = new JSpinner()
    maxSpinner.setValue(12)
    val computeButton = new JButton("Compute table")
    commandBar.add(minSpinner)
    commandBar.add(maxSpinner)
    commandBar.add(computeButton)
    commandBar.setMaximumSize(new Dimension(width, 30))
    rootPane.add(commandBar)
    val spacingPanel = new JPanel()
    spacingPanel.setMaximumSize(new Dimension(width, 15))
    rootPane.add(spacingPanel)

    def drawTable(removeOld: Boolean): Unit = {
      val newTable = generateJTable(minSpinner.getValue.asInstanceOf[Int], maxSpinner.getValue.asInstanceOf[Int])
      newTable.setDefaultRenderer(classOf[Any], new TableCellRenderer(){
        override def getTableCellRendererComponent(table: JTable, value: Any, isSelected: Boolean,
                                                   hasFocus: Boolean, row: Int, column: Int): Component = {
          val label = new JLabel(if (value == null) "" else value.toString)
          label.setForeground(if (row == 0 || column == 0) Color.blue else Color.black)
          label
        }
      })
      if (removeOld) rootPane.remove(rootPane.getComponentCount - 1)
      rootPane.add(newTable)
      rootPane.revalidate()
      rootPane.repaint()
    }

    computeButton.addActionListener(_ => drawTable(true))
    drawTable(removeOld = false)
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setPreferredSize(new Dimension(width, height))
    frame.pack()
    frame.setVisible(true)
  }

}
