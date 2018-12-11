package com.ing.baker.runtime

import java.util.UUID
import java.util.concurrent.{Executors, TimeUnit}

import akka.actor.ActorSystem
import com.ing.baker.BakerRuntimeTestBase
import com.ing.baker.recipe.TestRecipe.InitialEvent
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

class PerformanceTest extends BakerRuntimeTestBase {

  override def actorSystemName = "PerformanceTest"

//  CassandraLauncher.start(
//    new java.io.File("target/cassandra"),
//    CassandraLauncher.DefaultTestConfigResource,
//    clean = true,
//    port = 9042,
//    CassandraLauncher.classpathForResources("logback-test.xml")
//  )
//  Thread.sleep(10 * 1000)

  val cassandraConfig =
    s"""
       |
       |include "baker.conf"
       |
       |akka.persistence.journal.plugin = "cassandra-journal"
       |akka.persistence.snapshot-store.plugin = "cassandra-snapshot-store"
       |baker.actor.read-journal-plugin = "cassandra-query-journal"
       |
       |cassandra-journal.log-queries = off
       |cassandra-journal.keyspace-autocreate = true
       |cassandra-journal.tables-autocreate = true
       |
       |cassandra-snapshot-store.log-queries = off
       |cassandra-snapshot-store.keyspace-autocreate = true
       |cassandra-snapshot-store.tables-autocreate = true
       |
       |akka.loggers = ["akka.event.slf4j.Slf4jLogger"]
       |akka.loglevel = "DEBUG"
       |
       |
     """.stripMargin


  "Baker" should {

    "should process really fast" in {

      val akkaCassandraSystem = ActorSystem("Perf", ConfigFactory.parseString(cassandraConfig))

      import com.codahale.metrics.MetricRegistry

      val metrics = new MetricRegistry

      import com.codahale.metrics.ConsoleReporter

      val reporter: ConsoleReporter = ConsoleReporter.forRegistry(metrics)
        .convertRatesTo(TimeUnit.SECONDS)
        .convertDurationsTo(TimeUnit.MILLISECONDS).build

      reporter.start(1, TimeUnit.SECONDS)

      val (baker, recipeId) = setupBakerWithRecipe("TestRecipe")(akkaCassandraSystem)

      val nrOfBakes = 100 * 1000
      val nrOfThreads = 8

      val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(nrOfThreads))

      // warmup

      val processId = UUID.randomUUID().toString

      baker.bake(recipeId, processId)
      baker.processEvent(processId, InitialEvent("initialIngredient"))

      val bakeTimer = metrics.timer(MetricRegistry.name("PerformanceTest", "process"))

//      println("Starting stress testing")

      // stress testing
      (1 to nrOfBakes).foreach { _ =>

        executionContext.execute { () =>

          val time = bakeTimer.time()

          try {
            val processId = UUID.randomUUID().toString

            baker.bake(recipeId, processId)
            baker.processEvent(processId, InitialEvent("initialIngredient"))

          } finally {
            time.stop()
          }
        }
      }

      Thread.sleep(1000 * 60 * 2)
    }
  }
}
