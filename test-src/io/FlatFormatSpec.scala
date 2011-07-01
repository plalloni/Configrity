import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import configrity.Configuration
import configrity.ValueConverters._
import configrity.io.FlatFormat
import configrity.io.FlatFormat._


class FlatFormatSpec extends FlatSpec with ShouldMatchers{


  "The flat format" can "write and read an empty Configuration" in {
    val config = Configuration( Map() )
    fromText( toText( config ) ) should be (config)
  }

  it can "write and read a Configuration" in {
    val config = Configuration( 
      Map("foo"->"FOO", "bar"->"1234", "baz"->"on")
    )
    fromText( toText( config ) ) should be (config)

  }

}

class FlatFormatParserSpec extends FlatSpec with ShouldMatchers{


  def parse( s: String ) = Parser.parse(s)

  "The flat format parse" can "parse empty string" in {
    val config = parse( "" )
    config.data should be ('empty)
  }

  it can "parse a wellformed entry line" in {
    val config = parse( "foo = 2" )
    config[Int]("foo") should be (Some(2))
  }
 
  it should "ignore leading and trailing space" in {
    val config = parse( "    foo = 2 " )
    config[Int]("foo") should be (Some(2))
  } 

  it should "ignore extra spaces around the equal sign" in {
    val config = parse( "    foo     =      2 " )
    config[Int]("foo") should be (Some(2))
  } 

  it should "tolerate an equal sign without space around" in {
    val config = parse( "    foo=2 " )
    config[Int]("foo") should be (Some(2))
  }

  it can "parse several lines" in {
    val s = 
    """
    foo = true
    bar = 2
    baz = hello world
    """
    val config = parse( s )
    config[Boolean]("foo") should be (Some(true))
    config[Int]("bar") should be (Some(2))
    config[String]("baz") should be (Some("hello world"))
  }

  it can "parse several badly spaced lines" in {
    val s = 
    """
       foo  =true
    bar= 2        
                                 baz = hello world
     """
    val config = parse( s )
    config[Boolean]("foo") should be (Some(true))
    config[Int]("bar") should be (Some(2))
    config[String]("baz") should be (Some("hello world"))
  }

  it must "choke when encountering lines with two equals" in {
       val s = 
    """
    foo = true
    bar = 2
    baz = x = 2
    """
    intercept[ParserException] {
      val config = parse( s ) 
    }
  }

  it must "choke when encoutering a line without value" in {
       val s = 
    """
    foo = true
    bar = 
    baz = x 
    """
    intercept[ParserException] {
      val config = parse( s ) 
    }
  }

  it must "choke when encoutering a line without key" in {
       val s = 
    """
    foo = true
    = 2 
    baz = x 
    """
    intercept[ParserException] {
      val config = parse( s ) 
    }
  }

  it must "choke when encoutering a line without equals sign" in {
       val s = 
    """
    foo = true
    bar  2 
    baz = x 
    """
    intercept[ParserException] {
      val config = parse( s ) 
    }
  }

  it must "skip comments starting with a '#'" in {
       val s = 
    """
     # Example
    foo = true
    #bar = 2 
    baz = x 
    """
    val config = parse( s ) 
    config[Boolean]("foo") should be (Some(true))
    config[Int]("bar") should be (None)
    config[String]("baz") should be (Some("x"))
  }

}