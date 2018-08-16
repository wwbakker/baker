package com.ing.baker.runtime.actor.recipe_manager

import akka.actor.testkit.typed.scaladsl.{ActorTestKit, TestProbe}
import com.ing.baker.compiler.RecipeCompiler
import com.ing.baker.recipe.TestRecipe
import com.ing.baker.runtime.actor.recipe_manager.RecipeManagerTypedProtocol.{AddRecipe, AddRecipeResponse}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}
import org.slf4j.LoggerFactory


class RecipeManagerTypedSpec extends WordSpecLike with ActorTestKit with BeforeAndAfterAll {

  def actorSystemName = "RecipeManagerTypedSpec"


  val log = LoggerFactory.getLogger(classOf[RecipeManagerSpec])

  "The RecipeManagerTyped" should {
    "Add a recipe to the list when a AddRecipe message is received" in {

      val compiledRecipe = RecipeCompiler.compileRecipe(TestRecipe.getRecipe("AddRecipeRecipe"))

      val probe = TestProbe[AddRecipeResponse]
      val recipeManager = spawn(RecipeManagerTyped.behavior)
      recipeManager ! AddRecipe(compiledRecipe, probe.ref)
      probe.expectMessageType[AddRecipeResponse]


//      val recipeManagerKit = BehaviorTestKit(RecipeManagerTyped.behavior)
//      recipeManagerKit.run(AddRecipe(compiledRecipe, ))

      //      implicit val timeout: Timeout = 10 seconds
      //      val actorSystem = ActorSystem.apply(RecipeManagerTyped.behavior, actorSystemName)
      //      implicit val executionContext : ExecutionContext = actorSystem.executionContext
      //
      //      val compiledRecipe = RecipeCompiler.compileRecipe(TestRecipe.getRecipe("AddRecipeRecipe"))
      //      val recipeManager: Future[ActorRef[RecipeManagerCommand]] = actorSystem.systemActorOf(RecipeManagerTyped.behavior, s"recipeManager-${UUID.randomUUID().toString}")
      //      recipeManager.map {
      //        rm: ActorRef[RecipeManagerCommand] => {
      //          rm ? (ref => AddRecipe(compiledRecipe, ))
      //        }
      //
      //      }


      //        .map {
      //        rm: ActorRef[RecipeManagerCommand] =>
      //          rm ? (ref : ActorRef[AddRecipeResponse] => AddRecipe(compiledRecipe, ref))
      //      }

      //      val futureAddResult = recipeManager.ask(AddRecipe(compiledRecipe))(timeout)
      //      val recipeId: String = Await.result(futureAddResult, timeout) match {
      //        case AddRecipeResponse(x) => x
      //        case _ => fail("Adding recipe failed")
      //      }
      //
      //      val futureGetResult = recipeManager.ask(GetRecipe(recipeId))(timeout)
      //      Await.result(futureGetResult, timeout) match {
      //        case RecipeFound(recipe) => recipe
      //        case NoRecipeFound(_) => fail("Recipe not found")
      //        case _ => fail("Unknown response received")
      //      }
    }
  }

}
