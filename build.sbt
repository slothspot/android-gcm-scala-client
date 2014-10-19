name := "client"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.2"

javacOptions ++= Seq(
  "-deprecation", "-source", "1.6", "-target", "1.6", "-Xlint"
  )

scalacOptions ++= Seq(
    "-feature", "-deprecation", "-optimise", "-Xlint", "-Ywarn-dead-code", "-Ywarn-unused", "-Ywarn-unused-import",
    "-Ywarn-value-discard"
)

libraryDependencies ++= Seq(
    "com.android.support" % "support-v4" % "20.0.0",
    "com.google.android.gms" % "play-services" % "6.1.11"
  )

proguardOptions in Android ++= Seq("-dontobfuscate", "-dontoptimize")