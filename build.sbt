name := "HackerNews-Styler"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.jsoup" % "jsoup" % "1.7.3",
  jdbc,
  anorm,
  cache
)     

play.Project.playScalaSettings
