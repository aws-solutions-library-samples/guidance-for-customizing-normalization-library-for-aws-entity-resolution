import com.amazonaws.services.glue.GlueContext
import com.amazonaws.services.glue.util.GlueArgParser
import com.amazonaws.services.glue.util.Job
import com.amazonaws.services.glue.log.GlueLogger

import org.apache.spark.SparkContext
import org.apache.spark.sql.Dataset
import org.apache.spark.sql.Row

import org.apache.spark.sql.Column
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.types.{StructType, StructField, StringType}
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.functions.udf

import scala.collection.JavaConverters._
import org.apache.spark.sql.catalyst.encoders.RowEncoder


import com.amazon.lib.Normalizer;
import com.amazon.models.CustomerRecord;


object GlueApp extends Serializable {
  def main(sysArgs: Array[String]) {

    val spark: SparkContext = new SparkContext()
    val glueContext: GlueContext = new GlueContext(spark)
    val sparkSession: SparkSession = glueContext.getSparkSession
    val logger = new GlueLogger
    import sparkSession.implicits._

    // @params: [JOB_NAME]
    val args = GlueArgParser.getResolvedOptions(sysArgs, Seq("JOB_NAME","input_path","output_path").toArray)
    Job.init(args("JOB_NAME"), glueContext, args.asJava)

    val staticData = sparkSession.read          // read() returns type DataFrameReader
      .format("csv")
      .option("header", "true")
      .load(args("input_path"))


    staticData.printSchema()

    val normalizer = new Normalizer()

    /*
    val normalizeColumn = udf((value:String) => {
        return "shah"
        //normalizer.normalizeAddress(value, "")
    })

    staticData.withColumn("normalized_address", normalizeColumn($"address1")).show()
    */


    val schema = staticData.schema
    val normalizedFields = StructType(Array(
      StructField("normalized_address", StringType, true),
      StructField("normalized_city", StringType, true),
      StructField("normalized_state", StringType, true),
      StructField("normalized_zip", StringType, true),
      StructField("normalized_country", StringType, true),
      StructField("normalized_firstName", StringType, true),
      StructField("normalized_lastName", StringType, true),
      StructField("normalized_email", StringType, true),
      StructField("normalized_phone", StringType, true)
    ))
    val normalizedSchema = StructType(schema++normalizedFields)
    val encoder = RowEncoder(normalizedSchema)

    val transformedDF = staticData.map(row=>{
      logger.info("row: "+row.getClass)
      var record = CustomerRecord.builder().address((row.getAs[String]("address1")+" "+row.getAs[String]("address2")))
        .city(row.getAs("city"))
        .country(row.getAs("country_code"))
        .firstName(row.getAs("first_name"))
        .lastName(row.getAs("last_name"))
        .email(row.getAs("email_address"))
        .phone(row.getAs("phone_nbr"))
        .postal(row.getAs("zip_code"))
        .state(row.getAs("state_code"))
        .build();

      record = normalizer.normalizeRecord(record)
      logger.info("address: "+row.getAs("address1")+"\tnormalized: "+record.getAddress())
      Row.fromSeq(row.toSeq:+record.getAddress():+record.getCity():+record.getState():+record.getPostal():+record.getCountry():+record.getFirstName():+record.getLastName():+record.getEmail():+record.getPhone())
    }, encoder)

    transformedDF.write.option("header","true").format("csv").save(args("output_path"))

  }
}