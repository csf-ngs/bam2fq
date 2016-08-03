package at.ac.csf.ngs.bam2fq
import java.io.File
import htsjdk.samtools.SAMFileReader
import scala.collection.JavaConverters._
import htsjdk.samtools.util.CloserUtil
import htsjdk.samtools.fastq.FastqWriterFactory
import htsjdk.samtools.fastq.FastqRecord
import htsjdk.samtools.fastq.FastqWriter
import htsjdk.samtools.SAMRecord
import htsjdk.samtools.ValidationStringency._




class Bam(inFile: File, compress: Boolean, rename: Boolean) {
  
 
  def paired(): Boolean = {
     val input = new SAMFileReader(inFile)
     input.setValidationStringency(SILENT)
     val isPaired = input.iterator.asScala.take(1).map(_.getReadPairedFlag()).toList.headOption.getOrElse(false)
     CloserUtil.close(input)
     System.err.println(s"paired: $isPaired")
     isPaired
  }
  
 
  
  def convert(): String = {
        System.err.println("checking")
        val sp = if(paired){
            System.err.println("paired")
            convertPair
            "pair"
        } else {
           System.err.println("single")
           convertSingle
     	   "single" 
        }
        System.err.println(s"done conversion $sp")
        sp
   }

  def convertPair(){
    System.err.println("converting paired end")
    try{
       val factory = new FastqWriterFactory()
       val writer1 = factory.newWriter(new File(outName(1)))
       val writer2 = factory.newWriter(new File(outName(2)))
       val input = new SAMFileReader(inFile)
       input.setValidationStringency(SILENT)
       var barcodes = ""
       for(record <- input.iterator.asScala){
          if(record.getFirstOfPairFlag()){
            barcodes = Fastq.getBarcodes(record)
            writeRecord(record, 1, barcodes, writer1)
          } else {
            writeRecord(record, 2, barcodes, writer2)
          }
       }
       writer1.close()
       writer2.close()
       input.close()
    }catch{
       case e: Exception => System.err.println(e.getMessage); System.exit(1);
    
    }
  }
  
  
  def outName(read: Int): String = {
      if(rename){
        Fastq.illuminaName(inFile.getAbsolutePath, read, compress)
      }else{
        noRename(read)
      }
  }
  
  def noRename(read: Int): String = {
      val base = inFile.getCanonicalPath.replaceAll("""bam$""", "")
      base+read+"."+Fastq.suffix(compress)
  }
   
  
  def convertSingle(){
     val factory = new FastqWriterFactory()
     val writer = factory.newWriter(new File(outName(1)))
     val input = new SAMFileReader(inFile)
     input.setValidationStringency(SILENT)
     for(record <- input.iterator.asScala){  
          val barcodes = Fastq.getBarcodes(record)
      	  writeRecord(record, 1, barcodes, writer)
     }
     writer.close()
     input.close()
  }
  

  
  def writeRecord(read: SAMRecord, readNumber: Int, barcodes: String, writer: FastqWriter){
        val seqHeader = Fastq.generateReadName(read.getReadName, readNumber, barcodes)
   	    val readString = read.getReadString()
        val baseQualities = read.getBaseQualityString()
     	  writer.write(new FastqRecord(seqHeader, readString, "", baseQualities))
  }
  
  
}


object Fastq {
  val ReadPattern = """\w*:\d*:(\w*):(\d):\d*:\d*:\d*(#.*)?""".r
  
  def getFlowcellLane(readName: String): (String,Int,String) = {
      readName match {
        case ReadPattern(flowcell, lane, null) => (flowcell, lane.toInt, "")
        case ReadPattern(flowcell, lane, sam) => {
           val sams = sam.replaceAll("_","")
           (flowcell, lane.toInt, sams.substring(1))
        }
        case _ => throw new RuntimeException("could not parse flowcell and lane from read name: "+readName)
      }
  }
  
  
    //@<instrument>:<run number>:<flowcell ID>:<lane>:<tile>:<x-pos>:<y-pos> <read>:<is filtered>:<control number>:<index sequence>
  def generateSuffix(nr: Int, barcodes: String): String = nr+":N:0:"+barcodes 
  
  def generateReadPrefix(readName: String): String = {
      val hashPos = readName.indexOf("#")
      if(hashPos > -1){
         readName.substring(0, hashPos)
      } else {
         readName
      }
  }
  
  def generateReadName(readName: String, nr: Int, barcodes: String): String = {
      val suffix = generateSuffix(nr, barcodes)
      val prefix = generateReadPrefix(readName)
      prefix+" "+suffix
  }
   
  def bc(read: SAMRecord, tag: String): String = {
      val b = read.getAttribute(tag)
      if(b == null) "" else b.toString
  }
  
  def getBarcodes(read: SAMRecord): String = {
      val bc1 = bc(read, "BC")
      val bc2 = bc(read, "B2")
      if(bc1 != ""){
         if(bc2 != ""){
           bc1+"+"+bc2
         }else{
           bc1
         }
      }else{
        "1"
      }      
  }
  
   def readLine(bam: String): String = {
       val input = new SAMFileReader(new java.io.File(bam))
       input.setValidationStringency(SILENT)
       val record = input.iterator.asScala.next()
       val name = record.getReadName
       input.close()
       name
  }
  
  def suffix(compress: Boolean): String = if(compress) "fastq.gz" else "fastq"
   
  def illuminaName(bam: String, read: Int, compress: Boolean): String = {
      val readName = readLine(bam)
      val (fc,l,sam) = Fastq.getFlowcellLane(readName)
      s"${fc}${l}${sam}_S1_L00${l}_R${read}_001.${suffix(compress)}"
  }
  
}