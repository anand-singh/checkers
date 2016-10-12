package checkers.computer

object DefaultPrograms {

  object ids {
    val TrivialPlayer = "TrivialPlayer"
    val Medium = "Medium"
  }


  def registerAll(registry: ProgramRegistry): Unit = {

    def register(name: String, uniqueId: String, difficultyLevel: Int, factory: ProgramFactory): Unit = {
      val entry = ProgramRegistryEntry(name, uniqueId, difficultyLevel, factory)
      registry.register(entry)
    }

    register("Computer (Easiest)", ids.TrivialPlayer, 0, new TrivialPlayerFactory)

    val medium = {
      val params = SearchParameters(None, cycleLimit = Option(1000000), MoveSelectionMethodWeights.alwaysBestMove)
      val personality = new StaticPersonality(params)
      new ComputerPlayerFactory(personality)
    }

    register("Computer (Medium)", ids.Medium, 5, medium)
  }

}