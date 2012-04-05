package at.ac.csf.ngs.bam2fq
import java.io.File
import net.sf.samtools.SAMFileReader
import scala.collection.JavaConverters._
import net.sf.samtools.util.CloserUtil
import net.sf.picard.fastq.FastqWriterFactory
import net.sf.picard.fastq.FastqRecord
import net.sf.picard.fastq.FastqWriter
import net.sf.samtools.SAMRecord

class Bam(inFile: File, compress: Boolean) {

 
  def paired(): Option[Boolean] = {
     val input = new SAMFileReader(inFile)
     val isPaired = input.iterator.asScala.take(1).map(_.getReadPairedFlag()).toList.headOption
     CloserUtil.close(input)
     isPaired
  }
  
  def suffix(): String = if(compress) "fastq.gz" else "fastq"
  
  def convert(){
     paired.map{ p =>
	     if(p){
	       convertPair
	     }else{
	       convertSingle
	     }
     }
  }
  
  def convertPair(){
    val factory = new FastqWriterFactory()
    val writer1 = factory.newWriter(new File(base+"1."+suffix))
    val writer2 = factory.newWriter(new File(base+"2."+suffix))
    val input = new SAMFileReader(inFile)
    for(record <- input.iterator.asScala){
        if(record.getFirstOfPairFlag()){
          writeRecord(record, Some(1), writer1)
        }else{
          writeRecord(record, Some(2), writer2)
        }
    }
    writer1.close()
    writer2.close()
    input.close()
  }
  
  def base(): String = inFile.getCanonicalPath.replaceAll("""bam$""", "")
  
  def convertSingle(){
     val factory = new FastqWriterFactory()
     val writer = factory.newWriter(new File(base+suffix))
     val input = new SAMFileReader(inFile)
     for(record <- input.iterator.asScala){  
      	  writeRecord(record, None, writer)
     }
     writer.close()
     input.close()
  }
  
  def writeRecord(read: SAMRecord, readNumber: Option[Int], writer: FastqWriter){
        val seqHeader = readNumber.map(n => read.getReadName+"/"+n).getOrElse(read.getReadName)
   	    val readString = read.getReadString()
        val baseQualities = read.getBaseQualityString()
     	writer.write(new FastqRecord(seqHeader, readString, "", baseQualities))
  }
  
}