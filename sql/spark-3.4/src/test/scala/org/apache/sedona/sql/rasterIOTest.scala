/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.sedona.sql

import org.apache.commons.io.FileUtils
import org.apache.spark.sql.SaveMode
import org.locationtech.jts.geom.Geometry
import org.scalatest.{BeforeAndAfter, GivenWhenThen}

import java.io.File
import java.nio.file.Files
import scala.collection.mutable

class rasterIOTest extends TestBaseScala with BeforeAndAfter with GivenWhenThen {

  var rasterdatalocation: String = resourceFolder + "raster/"
  val tempDir: String = Files.createTempDirectory("sedona_raster_io_test_").toFile.getAbsolutePath

  describe("Raster IO test") {
    it("Should Pass geotiff loading without readFromCRS and readToCRS") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      assert(df.first().getAs[Geometry](1).toText == "POLYGON ((-13095782 4021226.5, -13095782 3983905, -13058822 3983905, -13058822 4021226.5, -13095782 4021226.5))")
      assert(df.first().getInt(2) == 517)
      assert(df.first().getInt(3) == 512)
      assert(df.first().getInt(5) == 1)
      val blackBand = df.first().getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff loading with readToCRS") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).option("readToCRS", "EPSG:4326").load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      assert(df.first().getAs[Geometry](1).toText == "POLYGON ((-117.64141128097314 33.94356351407699, -117.64141128097314 33.664978146501284, -117.30939395196258 33.664978146501284," +
        " -117.30939395196258 33.94356351407699, -117.64141128097314 33.94356351407699))")
      assert(df.first().getInt(2) == 517)
      assert(df.first().getInt(3) == 512)
      assert(df.first().getInt(5) == 1)
      val blackBand = df.first().getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff loading with readFromCRS") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).option("readFromCRS", "EPSG:4499").load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      assert(df.first().getAs[Geometry](1).toText == "POLYGON ((-13095782 4021226.5, -13095782 3983905, -13058822 3983905, -13058822 4021226.5, -13095782 4021226.5))")
      assert(df.first().getInt(2) == 517)
      assert(df.first().getInt(3) == 512)
      assert(df.first().getInt(5) == 1)
      val blackBand = df.first().getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff loading with readFromCRS and readToCRS") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).option("readFromCRS", "EPSG:4499").option("readToCRS", "EPSG:4326").load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      assert(df.first().getAs[Geometry](1).toText == "POLYGON ((-117.64141128097314 33.94356351407699, -117.64141128097314 33.664978146501284, -117.30939395196258 33.664978146501284," +
        " -117.30939395196258 33.94356351407699, -117.64141128097314 33.94356351407699))")
      assert(df.first().getInt(2) == 517)
      assert(df.first().getInt(3) == 512)
      assert(df.first().getInt(5) == 1)
      val blackBand = df.first().getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff loading with all read options") {
      var df = sparkSession.read.format("geotiff")
        .option("dropInvalid", true)
        .option("readFromCRS", "EPSG:4499")
        .option("readToCRS", "EPSG:4326")
        .option("disableErrorInCRS", true)
        .load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      assert(df.first().getAs[Geometry](1).toText == "POLYGON ((-117.64141128097314 33.94356351407699, -117.64141128097314 33.664978146501284, -117.30939395196258 33.664978146501284," +
        " -117.30939395196258 33.94356351407699, -117.64141128097314 33.94356351407699))")
      assert(df.first().getInt(2) == 517)
      assert(df.first().getInt(3) == 512)
      assert(df.first().getInt(5) == 1)
      val blackBand = df.first().getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("should pass RS_GetBand") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/")
      df = df.selectExpr(" image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 1, bands) as targetBand")
      assert(df.first().getAs[mutable.WrappedArray[Double]](0).length == 512 * 517)
    }

    it("should pass RS_Base64") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/")
      df = df.selectExpr("image.origin as origin", "ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 1, bands) as targetBand", "width", "height")
      df.createOrReplaceTempView("geotiff")
      df = sparkSession.sql("Select RS_base64(height, width, targetBand, RS_Array(height*width, 0.0), RS_Array(height*width, 0.0)) as encodedstring from geotiff")
      assert(df.first().getAs[String](0).startsWith("iVBORw"))
    }

    it("should pass RS_HTML") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/")
      df = df.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 1, bands) as targetBand", "width","height")
      df.createOrReplaceTempView("geotiff")
      df = sparkSession.sql("Select RS_base64(height, width, targetBand, RS_Array(height*width, 0.0), RS_Array(height*width, 0.0)) as encodedstring from geotiff")
      df = df.selectExpr("RS_HTML(encodedstring, '300') as htmlstring" )
      assert(df.first().getAs[String](0).startsWith("<img src=\"data:image/png;base64,iVBORw"))
      assert(df.first().getAs[String](0).endsWith("/>"))    }

    it("should pass RS_GetBand for length of Band 2") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/test3.tif")
      df = df.selectExpr(" image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 2, bands) as targetBand")
      assert(df.first().getAs[mutable.WrappedArray[Double]](0).length == 32 * 32)
    }

    it("should pass RS_GetBand for elements of Band 2") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/test3.tif")
      df = df.selectExpr(" image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 2, bands) as targetBand")
      assert(df.first().getAs[mutable.WrappedArray[Double]](0)(1) == 956.0)
    }

    it("should pass RS_GetBand for elements of Band 4") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(resourceFolder + "raster/test3.tif")
      df = df.selectExpr(" image.data as data", "image.nBands as bands")
      df = df.selectExpr("RS_GetBand(data, 4, bands) as targetBand")
      assert(df.first().getAs[mutable.WrappedArray[Double]](0)(2) == 0.0)
    }

    it("Should Pass geotiff file writing with coalesce") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).option("readToCRS", "EPSG:4326").load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","image.geometry as geometry", "image.height as height", "image.width as width", "image.data as data", "image.nBands as nBands")
      val savePath = tempDir + "/raster-written/"
      df.coalesce(1).write.mode("overwrite").format("geotiff").save(savePath)

      var loadPath = savePath
      val tempFile = new File(loadPath)
      val fileList = tempFile.listFiles()
      for (i <- 0 until fileList.length) {
        if (fileList(i).isDirectory) loadPath = fileList(i).getAbsolutePath
      }

      var dfWritten = sparkSession.read.format("geotiff").option("dropInvalid", true).load(loadPath)
      dfWritten = dfWritten.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      val rowFirst = dfWritten.first()
      assert(rowFirst.getInt(2) == 517)
      assert(rowFirst.getInt(3) == 512)
      assert(rowFirst.getInt(5) == 1)

      val blackBand = rowFirst.getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff file writing with writeToCRS") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","image.geometry as geometry", "image.height as height", "image.width as width", "image.data as data", "image.nBands as nBands")
      val savePath = tempDir + "/raster-written/"
      df.coalesce(1).write.mode("overwrite").format("geotiff").option("writeToCRS", "EPSG:4499").save(savePath)

      var loadPath = savePath
      val tempFile = new File(loadPath)
      val fileList = tempFile.listFiles()
      for (i <- 0 until fileList.length) {
        if (fileList(i).isDirectory) loadPath = fileList(i).getAbsolutePath
      }

      var dfWritten = sparkSession.read.format("geotiff").option("dropInvalid", true).load(loadPath)
      dfWritten = dfWritten.selectExpr("image.origin as origin","ST_GeomFromWkt(image.geometry) as Geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      val rowFirst = dfWritten.first()
      assert(rowFirst.getInt(2) == 517)
      assert(rowFirst.getInt(3) == 512)
      assert(rowFirst.getInt(5) == 1)

      val blackBand = rowFirst.getAs[mutable.WrappedArray[Double]](4)
      val line1 = blackBand.slice(0, 512)
      val line2 = blackBand.slice(512, 1024)
      assert(line1(0) == 0.0) // The first value at line 1 is black
      assert(line2(159) == 0.0 && line2(160) == 123.0) // In the second line, value at 159 is black and at 160 is not black
    }

    it("Should Pass geotiff file writing without coalesce") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","image.geometry as geometry", "image.height as height", "image.width as width", "image.data as data", "image.nBands as nBands")
      val savePath = tempDir + "/raster-written/"
      df.write.mode("overwrite").format("geotiff").save(savePath)

      var imageCount = 0
      def getFile(loadPath: String): Unit ={
        val tempFile = new File(loadPath)
        val fileList = tempFile.listFiles()
        if (fileList == null) return
        for (i <- 0 until fileList.length) {
          if (fileList(i).isDirectory) getFile(fileList(i).getAbsolutePath)
          else if (fileList(i).getAbsolutePath.endsWith(".tiff") || fileList(i).getAbsolutePath.endsWith(".tif")) imageCount += 1
        }
      }

      getFile(savePath)
      assert(imageCount == 3)
    }

    it("Should Pass geotiff file writing with nested schema") {
      val df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      val savePath = resourceFolder + "raster-written/"
      df.write.mode("overwrite").format("geotiff").save(savePath)

      var imageCount = 0
      def getFile(loadPath: String): Unit ={
        val tempFile = new File(loadPath)
        val fileList = tempFile.listFiles()
        if (fileList == null) return
        for (i <- 0 until fileList.length) {
          if (fileList(i).isDirectory) getFile(fileList(i).getAbsolutePath)
          else if (fileList(i).getAbsolutePath.endsWith(".tiff") || fileList(i).getAbsolutePath.endsWith(".tif")) imageCount += 1
        }
      }

      getFile(savePath)
      assert(imageCount == 3)
    }

    it("Should Pass geotiff file writing with renamed fields") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as source","image.geometry as geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      val savePath = resourceFolder + "raster-written/"
      df.write
        .mode("overwrite")
        .format("geotiff")
        .option("fieldOrigin", "source")
        .option("fieldGeometry", "geom")
        .option("fieldNBands", "bands")
        .save(savePath)

      var imageCount = 0
      def getFile(loadPath: String): Unit ={
        val tempFile = new File(loadPath)
        val fileList = tempFile.listFiles()
        if (fileList == null) return
        for (i <- 0 until fileList.length) {
          if (fileList(i).isDirectory) getFile(fileList(i).getAbsolutePath)
          else if (fileList(i).getAbsolutePath.endsWith(".tiff") || fileList(i).getAbsolutePath.endsWith(".tif")) imageCount += 1
        }
      }

      getFile(savePath)
      assert(imageCount == 3)
    }

    it("Should Pass geotiff file writing with nested schema and renamed fields") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image as tiff_image")
      val savePath = resourceFolder + "raster-written/"
      df.write
        .mode("overwrite")
        .format("geotiff")
        .option("fieldImage", "tiff_image")
        .save(savePath)

      var imageCount = 0
      def getFile(loadPath: String): Unit ={
        val tempFile = new File(loadPath)
        val fileList = tempFile.listFiles()
        if (fileList == null) return
        for (i <- 0 until fileList.length) {
          if (fileList(i).isDirectory) getFile(fileList(i).getAbsolutePath)
          else if (fileList(i).getAbsolutePath.endsWith(".tiff") || fileList(i).getAbsolutePath.endsWith(".tif")) imageCount += 1
        }
      }

      getFile(savePath)
      assert(imageCount == 3)
    }

    it("Should Pass geotiff file writing with converted geometry") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as source","ST_GeomFromWkt(image.geometry) as geom", "image.height as height", "image.width as width", "image.data as data", "image.nBands as bands")
      val savePath = resourceFolder + "raster-written/"
      df.write
        .mode("overwrite")
        .format("geotiff")
        .option("fieldOrigin", "source")
        .option("fieldGeometry", "geom")
        .option("fieldNBands", "bands")
        .save(savePath)

      var imageCount = 0
      def getFile(loadPath: String): Unit ={
        val tempFile = new File(loadPath)
        val fileList = tempFile.listFiles()
        if (fileList == null) return
        for (i <- 0 until fileList.length) {
          if (fileList(i).isDirectory) getFile(fileList(i).getAbsolutePath)
          else if (fileList(i).getAbsolutePath.endsWith(".tiff") || fileList(i).getAbsolutePath.endsWith(".tif")) imageCount += 1
        }
      }

      getFile(savePath)
      assert(imageCount == 3)
    }

    it("Should Pass geotiff file writing with handling invalid schema") {
      var df = sparkSession.read.format("geotiff").option("dropInvalid", true).load(rasterdatalocation)
      df = df.selectExpr("image.origin as origin","image.geometry as geometry", "image.height as height", "image.width as width", "image.data as data")
      val savePath = resourceFolder + "raster-written/"

      try {
        df.write
          .mode("overwrite")
          .format("geotiff")
          .option("fieldImage", "tiff_image")
          .save(savePath)
      }
      catch {
        case e: IllegalArgumentException => {
          assert(e.getMessage == "Invalid GeoTiff Schema")
        }
      }
    }

    it("should read geotiff using binary source and write geotiff back to disk using raster source") {
      var rasterDf = sparkSession.read.format("binaryFile").load(rasterdatalocation)
      val rasterCount = rasterDf.count()
      rasterDf.write.format("raster").mode(SaveMode.Overwrite).save(tempDir + "/raster-written")
      rasterDf = sparkSession.read.format("binaryFile").load(tempDir + "/raster-written/*")
      rasterDf = rasterDf.selectExpr("RS_FromGeoTiff(content)")
      assert(rasterDf.count() == rasterCount)
    }

    it("should read and write geotiff using given options") {
      var rasterDf = sparkSession.read.format("binaryFile").load(rasterdatalocation)
      val rasterCount = rasterDf.count()
      rasterDf.write.format("raster").option("rasterField", "content").option("fileExtension", ".tiff").option("pathField", "path").mode(SaveMode.Overwrite).save(tempDir + "/raster-written")
      rasterDf = sparkSession.read.format("binaryFile").load(tempDir + "/raster-written/*")
      rasterDf = rasterDf.selectExpr("RS_FromGeoTiff(content)")
      assert(rasterDf.count() == rasterCount)
    }

    it("should read and write via RS_FromGeoTiff and RS_AsGeoTiff") {
      var df = sparkSession.read.format("binaryFile").load(rasterdatalocation)
      var rasterDf = df.selectExpr("RS_FromGeoTiff(content) as raster", "path").selectExpr("RS_AsGeoTiff(raster) as content", "path")
      val rasterCount = rasterDf.count()
      rasterDf.write.format("raster").option("rasterField", "content").option("fileExtension", ".tiff").option("pathField", "path").mode(SaveMode.Overwrite).save(tempDir + "/raster-written")
      df = sparkSession.read.format("binaryFile").load(tempDir + "/raster-written/*")
      rasterDf = df.selectExpr("RS_FromGeoTiff(content)")
      assert(rasterDf.count() == rasterCount)
    }

    it("should handle null") {
      var df = sparkSession.read.format("binaryFile").load(rasterdatalocation)
      var rasterDf = df.selectExpr("RS_FromGeoTiff(null) as raster", "length").selectExpr("RS_AsGeoTiff(raster) as content", "length")
      val rasterCount = rasterDf.count()
      rasterDf.write.format("raster").mode(SaveMode.Overwrite).save(tempDir + "/raster-written")
      df = sparkSession.read.format("binaryFile").load(tempDir + "/raster-written/*")
      rasterDf = df.selectExpr("RS_FromGeoTiff(content)")
      assert(rasterCount == 3)
      assert(rasterDf.count() == 0)
    }

    it("should read RS_FromGeoTiff and write RS_AsArcGrid") {
      var df = sparkSession.read.format("binaryFile").load(resourceFolder + "raster_geotiff_color/*")
      var rasterDf = df.selectExpr("RS_FromGeoTiff(content) as raster", "path").selectExpr("RS_AsArcGrid(raster, 1) as content", "path")
      val rasterCount = rasterDf.count()
      rasterDf.write.format("raster").option("rasterField", "content").option("fileExtension", ".asc").option("pathField", "path").mode(SaveMode.Overwrite).save(tempDir + "/raster-written")
      df = sparkSession.read.format("binaryFile").load(tempDir + "/raster-written/*")
      rasterDf = df.selectExpr("RS_FromArcInfoAsciiGrid(content)")
      assert(rasterDf.count() == rasterCount)
    }
  }

  override def afterAll(): Unit = FileUtils.deleteDirectory(new File(tempDir))
}