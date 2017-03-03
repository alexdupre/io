package sbt.io

import java.nio.file.{ Files, Path => NioPath }

import org.scalatest._
import Path._
import sbt.io.syntax._

class PathMapperSpec extends fixture.FlatSpec with Matchers {

  type FixtureParam = NioPath

  "rebase | flat" should "copy resource mappings correctly" in { tempDirectory =>
    val base = tempDirectory.toFile

    val files = Seq(base / "src" / "main" / "resources" / "scalac-plugin.xml")
    val dirs = Seq(
      base / "src" / "main" / "resources",
      base / "target" / "scala-2.11" / "resource_managed" / "main"
    )
    val target = base / "target" / "scala-2.11" / "classes"

    val mappings = (files --- dirs) pair (rebase(dirs, target) | flat(target))

    mappings shouldBe Seq(
      base / "src" / "main" / "resources" / "scalac-plugin.xml" ->
        base / "target" / "scala-2.11" / "classes" / "scalac-plugin.xml"
    )
  }

  "directory" should "create mappings including the baseDirectory" in { tempDirectory =>

    val nestedFile1 = Files.createFile(tempDirectory resolve "file1").toFile
    val nestedFile2 = Files.createFile(tempDirectory resolve "file2").toFile
    val nestedDir = Files.createDirectory(tempDirectory resolve "dir1")
    val nestedDirFile = Files.createDirectory(nestedDir resolve "dir1-file1").toFile

    val mappings = Path.directory(tempDirectory.toFile)

    mappings should contain theSameElementsAs List[(File, String)](
      tempDirectory.toFile -> s"${tempDirectory.getFileName}",
      nestedFile1 -> s"${tempDirectory.getFileName}/file1",
      nestedFile2 -> s"${tempDirectory.getFileName}/file2",
      nestedDir.toFile -> s"${tempDirectory.getFileName}/dir1",
      nestedDirFile -> s"${tempDirectory.getFileName}/dir1/dir1-file1"
    )
  }

  "contentOf" should "create mappings excluding the baseDirectory" in { tempDirectory =>

    val nestedFile1 = Files.createFile(tempDirectory resolve "file1").toFile
    val nestedFile2 = Files.createFile(tempDirectory resolve "file2").toFile
    val nestedDir = Files.createDirectory(tempDirectory resolve "dir1")
    val nestedDirFile = Files.createDirectory(nestedDir resolve "dir1-file1").toFile

    val mappings = Path.contentOf(tempDirectory.toFile)

    mappings should contain theSameElementsAs List[(File, String)](
      nestedFile1 -> s"file1",
      nestedFile2 -> s"file2",
      nestedDir.toFile -> s"dir1",
      nestedDirFile -> s"dir1/dir1-file1"
    )
  }

  override protected def withFixture(test: OneArgTest): Outcome = {
    val tmpDir = Files.createTempDirectory("path-mappings")
    try {
      withFixture(test.toNoArgTest(tmpDir))
    } finally {
      // cleanup an delete the temp directory
      IO.delete(tmpDir.toFile)
    }
  }
}
