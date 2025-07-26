# Harmony

**Harmony** is a Minecraft mod for NeoForge 1.21.1 designed to act as a central coordinator and mediator between other mods—especially those that modify world generation (biomes, structures, etc.). Harmony ensures stability, compatibility, and performance by managing how different mods interact with the Minecraft world, preventing conflicts and crashes.

## Features

- **Central Mod Coordination:** Acts as a "boss" mod, managing how other mods interact with world generation.
- **Conflict Resolution:** Prevents overlapping biomes, structures, and chunk modifications.
- **Dynamic Assignment:** Assigns worldgen responsibilities (biome/chunk/structure) to mods based on rules or configuration.
- **Open API:** Other mods can register and communicate with Harmony for safe, coordinated interaction.
- **Performance Guard:** Detects and prevents laggy or recursive operations from mods that could destabilize the game.
- **User Configuration:** Powerful config file lets users fine-tune which mods control what aspects of world generation.

## Example Use Case

If you have multiple world generation mods (e.g., four different biome or structure mods), Harmony will:
- Assign each mod specific biomes or chunks to modify, so they don’t overwrite or conflict with each other.
- Ensure structures from different mods do not overlap.
- Prevent crashes and performance drops from worldgen conflicts.

## Getting Started

### Requirements

- Minecraft **1.21.1** (Java Edition)
- [NeoForge](https://neoforged.net/) mod loader
- Java 17 or newer

### Installation

1. Download the latest Harmony mod jar from the [Releases](https://github.com/YOUR_USERNAME/Harmony/releases) page.
2. Place the jar in your Minecraft `mods` folder.
3. Configure Harmony using the provided config file for your desired worldgen rules.

### For Mod Developers

Harmony provides an API that allows your mod to:
- Register worldgen features with Harmony.
- Request or release control over specific biomes/chunks/structures.
- Listen for events about worldgen permissions and assignments.

**See the [API Documentation](docs/API.md) for details and examples.**

## Contributing

Contributions, suggestions, and feature requests are welcome! Please open an issue or pull request.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

---

*Harmony: Making mods play nice together!*
