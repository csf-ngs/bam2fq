package at.ac.csf.ngs.bam2fq

import javax.swing.UIManager
import java.io.File
import javax.swing.JFrame
import java.awt.event.ActionListener
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JTextArea
import scala.collection.mutable.ArrayBuffer
import javax.swing.SwingWorker
import scala.actors.threadpool.ExecutorService
import java.util.concurrent.Executors
import javax.swing.JTextPane
import javax.swing.JMenuBar
import javax.swing.JMenu
import javax.swing.JMenuItem
import java.awt.event.KeyEvent
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter
import javax.swing.JOptionPane

case class ConversionFile(file: File, converted: Boolean){
  def forDisplay: String = file.getCanonicalPath()+" "+(if(converted) "converted" else "")  
}

class ConverterWorker(bam: File, index: Int, relabel: (Int) => Int ) extends SwingWorker[Unit,Unit](){
   	override def doInBackground() {
	      val converter = new Bam(bam, true)
	      converter.convert()
	      Thread.sleep(1000)
   	}
	override def done(){
	   relabel(index)
	}
}


class Bam2FqConverter extends JFrame("Bam To Fastq") {
  val button = new JButton("convert")
  val filesArea = new JTextPane()
  filesArea.setEditable(false)
  var filesList = new ArrayBuffer[ConversionFile]()
  val bar = new JMenuBar()
  val menu = new JMenu("File")
  bar.add(menu)
  val addFiles = new JMenuItem("Add Files", KeyEvent.VK_A)
  addFiles.addActionListener(new ActionListener(){
    override def actionPerformed(e: ActionEvent){
        val chooser = new JFileChooser()
        class BamFilter extends FileFilter {
           override def accept(file: File) = file.getName.endsWith(".bam") || file.isDirectory()
           override def getDescription() = "bam files"
        }
      	chooser.setFileFilter(new BamFilter)
        val returnVal = chooser.showOpenDialog(Bam2FqConverter.this)
        if(returnVal == JFileChooser.APPROVE_OPTION) {
        	val file = chooser.getSelectedFile()
        	if(!file.getName.endsWith("bam")){
        	  JOptionPane.showMessageDialog(Bam2FqConverter.this,
        	      "Only bam files will be converted", "wrong file",   
        	      JOptionPane.ERROR_MESSAGE)
        	}else{
        	   filesList.append(new ConversionFile(file, false))
        	   filesArea.setText(filesList.map(_.forDisplay).mkString("\n"))
        	}
        }
    }
  })
  
  val quit = new JMenuItem("Quit", KeyEvent.VK_Q)
  quit.addActionListener(new ActionListener(){
    override def actionPerformed(e: ActionEvent){
        System.exit(0)
    }
  })
  menu.add(addFiles)
  menu.add(quit)
  setJMenuBar(bar)

  
  getContentPane().add( new javax.swing.JScrollPane( filesArea ), java.awt.BorderLayout.CENTER )
  getContentPane().add(button, java.awt.BorderLayout.SOUTH)
  val drop = new FileDrop( null, filesArea, /*dragBorder,*/ 
        new FileDrop.Listener(){
           override def filesDropped(files: Array[File]){
                files.foreach{ f => 
                  val path = f.getCanonicalPath()
                  if(path.endsWith(".bam")){
                	  filesList.append(new ConversionFile(f, false))
                  }
                }
                filesArea.setText(filesList.map(_.forDisplay).mkString("\n"))
           }
  })
  button.addActionListener(new ActionListener(){

       override def actionPerformed(e: ActionEvent){
	       val array = filesList.toArray
	       def relabel(index: Int): Int = {
		       array(index) = array(index).copy(converted = true)
		       filesArea.setText(array.map(_.forDisplay).mkString("\n"))
		       index
		   } 
	       val executor = Executors.newSingleThreadExecutor()
	       for((bam,index) <- filesList.zipWithIndex){
	          if(!bam.converted){
		          val worker = new ConverterWorker(bam.file, index, relabel)
		          executor.execute(worker)
	          }
	       }     
	    }}
  )
 
       

  
}

object Main{
  
  def main(args: Array[String]){
     val converter = new Bam2FqConverter()
     converter.setSize(600,300)
     converter.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE )
     converter.setVisible(true)
  }
  
}

