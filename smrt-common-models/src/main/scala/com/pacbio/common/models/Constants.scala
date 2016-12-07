package com.pacbio.common.models

import java.util.Properties

/**
  *
  * Created by mkocher on 10/13/15.
  */
trait Constants {
  // Global DataSet "version" that every tool should use the write a DataSet
  final val DATASET_VERSION = "4.0.0"
  // Perforce CHANGELIST that was used to generate the XSDs
  final val XSD_CHANGELIST = "189211"

  val SMRTFLOW_VERSION = {
    val files = getClass().getClassLoader().getResources("version.properties")
    if (files.hasMoreElements) {
      val in = files.nextElement().openStream()
      try {
        val prop = new Properties
        prop.load(in)
        prop.getProperty("version").replace("SNAPSHOT", "") + prop.getProperty("sha1").substring(0, 7)
      }
      finally {
        in.close()
      }
    }
    else {
      "unknown version"
    }
  }
}

object Constants extends Constants

