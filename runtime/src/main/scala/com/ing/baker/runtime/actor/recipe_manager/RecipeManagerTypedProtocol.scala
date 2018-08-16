package com.ing.baker.runtime.actor.recipe_manager

import akka.actor.typed.ActorRef
import com.ing.baker.il.CompiledRecipe
import com.ing.baker.runtime.actor.InternalBakerMessage

object RecipeManagerTypedProtocol {

  // Commands
  sealed trait RecipeManagerCommand extends InternalBakerMessage

  case class AddRecipe(compiledRecipe: CompiledRecipe, replyTo: ActorRef[AddRecipeResponse]) extends RecipeManagerCommand

  case class GetRecipe(recipeId: String, replyTo: ActorRef[GetRecipeResponse]) extends RecipeManagerCommand

  case class GetAllRecipes(replyTo: ActorRef[AllRecipes]) extends RecipeManagerCommand


  // Responses
  sealed trait RecipeManagerResponse extends InternalBakerMessage

  case class AddRecipeResponse(recipeId: String) extends RecipeManagerResponse

  sealed trait GetRecipeResponse extends RecipeManagerResponse

  case class RecipeFound(compiledRecipe: CompiledRecipe) extends GetRecipeResponse

  case class NoRecipeFound(recipeId: String) extends GetRecipeResponse

  case class AllRecipes(compiledRecipes: Map[String, CompiledRecipe]) extends RecipeManagerResponse

  // Events
  sealed trait RecipeManagerTypedEvent

  case class RecipeAdded(recipeId: String, compiledRecipe: CompiledRecipe) extends RecipeManagerTypedEvent


}

