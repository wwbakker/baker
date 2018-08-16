package com.ing.baker.runtime.actor.recipe_manager

import java.util.UUID

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.persistence.typed.scaladsl.{Effect, PersistentBehaviors}
import com.ing.baker.il.CompiledRecipe
import com.ing.baker.runtime.actor.recipe_manager.RecipeManagerTypedProtocol._

//
//object RecipeManager {
//
//  //Events
//  //When a recipe is added
//}

case class State(compiledRecipes: Map[String, CompiledRecipe] = Map[String, CompiledRecipe]()) {
  def hasCompiledRecipe(compiledRecipe: CompiledRecipe): Option[String] =
    compiledRecipes.find(_._2 == compiledRecipe).map(_._1)

  def addRecipe(recipeId: String, compiledRecipe: CompiledRecipe): State =
    State(compiledRecipes + (recipeId -> compiledRecipe))
}


object RecipeManagerTyped {


  private val commandHandler: (ActorContext[RecipeManagerCommand], State, RecipeManagerCommand) ⇒ Effect[RecipeManagerTypedEvent, State] = (ctx, state, cmd) ⇒
    cmd match {
      case AddRecipe(compiledRecipe, replyTo) => {
        val foundRecipe = state.hasCompiledRecipe(compiledRecipe)
        if (foundRecipe.isEmpty) {
          val recipeId = UUID.randomUUID().toString

          Effect.persist(RecipeAdded(recipeId, compiledRecipe)).andThen { newState =>
            // @todo Find way to broadcast
            //              ctx.system.eventStream.publish(
            //                com.ing.baker.runtime.core.events.RecipeAdded(compiledRecipe.name, recipeId, System.currentTimeMillis(), compiledRecipe))
            replyTo ! AddRecipeResponse(recipeId)
          }
        }
        else {
          replyTo ! AddRecipeResponse(foundRecipe.get)
          Effect.none
        }
      }

      case GetRecipe(recipeId, replyTo) => {
        state.compiledRecipes.get(recipeId) match {
          case Some(compiledRecipe) => replyTo ! RecipeFound(compiledRecipe)
          case None => replyTo ! NoRecipeFound(recipeId)
        }

        Effect.none
      }

      case GetAllRecipes(replyTo) => {
        replyTo ! AllRecipes(state.compiledRecipes)
        Effect.stop
      }

      case _ ⇒
        Effect.unhandled
    }


  private val eventHandler: (State, RecipeManagerTypedEvent) ⇒ State = { (state, event) ⇒
    event match {
      case RecipeAdded(recipeId, compiledRecipe) ⇒
        state.addRecipe(recipeId, compiledRecipe)

    }
  }


  val behavior: Behavior[RecipeManagerCommand] =
    PersistentBehaviors.receive[RecipeManagerCommand, RecipeManagerTypedEvent, State](
      persistenceId = "recipe-manager", // @todo find correct self.path.name
      emptyState = State(),
      commandHandler = commandHandler,
      eventHandler = eventHandler
    )

}
