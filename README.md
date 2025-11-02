# SWCCore Addon System

A powerful, flexible addon system for Minecraft Spigot plugins built with Kotlin. Load, manage, and hot-reload addons without restarting your server.

## 🌟 Features

- ✅ **Hot Loading** - Load addons without restarting the server
- ✅ **Hot Unloading** - Unload and reload addons on-the-fly
- ✅ **Dynamic Commands** - Register commands without plugin.yml
- ✅ **Event System** - Full Bukkit event support
- ✅ **Dependency Management** - Addons can depend on other addons
- ✅ **State Management** - Track addon states (Loaded, Enabled, Disabled, etc.)
- ✅ **Permission System** - Fine-grained permission control
- ✅ **YAML Configuration** - Easy-to-use manifest.yml format
- ✅ **Automatic Scanning** - Detects new addons in the folder

## 📋 Table of Contents

- [Installation](#installaion)
- [Commands](#commands)
- [Permissions](#permissions)
- [Creating an Addon](#creating-an-addon)
- [Addon Lifecycle](#addon-lifecycle)
- [API Reference](#api-reference)
- [manifest.yml Format](#manifestyml-format)
- [Examples](#examples)
- [Troubleshooting](#troubleshooting)

---

## 🚀 Installation

### For Server Administrators

1. Place `SWCCore.jar` in your server's `plugins/` folder
2. Start the server (this creates the addon directory)
3. Place addon JARs in `plugins/SWCCore/addons/`
4. Use `/addon list` to see available addons
5. Use `/addon load <filename>` to load new addons
6. Use `/addon enable <id>` to enable them

### For Developers

See [Creating an Addon](#creating-an-addon) section below.

---

## 🎮 Commands

All commands require the base permission `swccore.addon`.

### `/addon`
Shows available addon management commands.

### `/addon list`
Lists all addons with their current status.

**Status Indicators:**
- §a● `ENABLED` - Addon is running
- §7● `DISABLED` - Addon is loaded but disabled
- §e● `LOADED` - Addon is loaded but not enabled
- §b○ `UNLOADED` - JAR exists in folder but not loaded
- §c✗ `FAILED` - Addon failed to load (shows error)

**Example Output:**
```
=== Addons (5) ===
§a● ExampleAddon v1.0.0 [ENABLED] (example-addon)
§7● TestAddon v2.1.0 [DISABLED] (test-addon)
§b○ NewAddon v1.5.0 [UNLOADED]
   File: NewAddon-1.5.0.jar - Use /addon load NewAddon-1.5.0.jar
§c✗ BrokenAddon.jar [FAILED]
   Error: does not have a manifest.yml file
```

### `/addon info <id>`
Shows detailed information about a specific addon.

**Example:**
```
/addon info example-addon
```

**Output:**
```
=== Example Addon ===
ID: example-addon
Version: 1.0.0
Authors: YourName
Description: An example addon that demonstrates the API
Status: §aENABLED
```

### `/addon load <filename>`
Loads an addon from the addons folder without restarting the server.

**Example:**
```
/addon load MyAddon-1.0.0.jar
```

### `/addon unload <id>`
Unloads an addon from memory (disables it first if needed).

**Example:**
```
/addon unload example-addon
```

### `/addon enable <id>`
Enables a loaded addon.

**Example:**
```
/addon enable example-addon
```

### `/addon disable <id>`
Disables a running addon without unloading it.

**Example:**
```
/addon disable example-addon
```

### `/addon reload [id]`
Reloads one or all addons.

**Examples:**
```
/addon reload                  # Reload all addons
/addon reload example-addon    # Reload specific addon
```

---

## 🔒 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `swccore.addon` | Full addon management (includes all below) | op |
| `swccore.addon.list` | View list of addons | op |
| `swccore.addon.info` | View addon details | op |
| `swccore.addon.load` | Load new addons | op |
| `swccore.addon.unload` | Unload addons | op |
| `swccore.addon.enable` | Enable addons | op |
| `swccore.addon.disable` | Disable addons | op |
| `swccore.addon.reload` | Reload addons | op |

### Permission Examples (LuckPerms)

```bash
# Grant all addon permissions
/lp user Steve permission set swccore.addon

# Grant only viewing permissions
/lp user Bob permission set swccore.addon.list
/lp user Bob permission set swccore.addon.info

# Grant management permissions to a group
/lp group admin permission set swccore.addon

# Revoke specific permission
/lp user Steve permission unset swccore.addon.unload
```

---

## 🛠️ Creating an Addon

### Prerequisites

- Java 17 or higher
- Kotlin 1.9.22 or higher
- Maven or Gradle
- SWCCore.jar (for compilation)

### Project Setup (Maven)

**1. Create `pom.xml`:**

Add this dep to your project
```xml
<dependency>
    <groupId>org.xs-t</groupId>
    <artifactId>swc-core</artifactId>
    <version>0.0.6-SNAPSHOT</version>
    <scope>provided</scope>
</dependency>
```

**2. Create directory structure:**

```
my-addon/
├── pom.xml
└── src/
    └── main/
        ├── kotlin/
        │   └── com/example/myaddon/
        │       ├── MyAddon.kt
        │       ├── commands/
        │       └── listeners/
        └── resources/
            └── manifest.yml
```

**3. Create `manifest.yml` inside the resources folder:**

```yaml
# Required fields
name: MyAddon
version: 1.0.0
main: com.example.myaddon.MyAddon
author: YourName
description: A cool addon for SWCCore

# Optional fields
authors:
  - YourName
  - Contributor

dependencies: [] # You'd put another dependency's id

website: https://example.com
```

**4. Create your main addon class:**

```kotlin
package com.example.myaddon

import org.crewco.swccore.api.addon.AbstractAddon
import org.bukkit.plugin.Plugin

class MyAddon(plugin: Plugin) : AbstractAddon(plugin) {

    override val id = "my-addon"
    override val name = "My Addon"
    override val version = "1.0.0"
    override val authors = listOf("YourName")
    override val description = "A cool addon"

    override fun onLoad() {
        super.onLoad()
        logInfo("Loading...")
    }

    override fun onEnable() {
        super.onEnable()

        // Register commands
        registerCommand("mycommand") { sender, _, _, _ ->
            sender.sendMessage("§aHello from MyAddon!")
            true
        }

        logInfo("Enabled!")
    }

    override fun onDisable() {
        super.onDisable()
        logInfo("Disabled!")
    }
}
```

**5. Build your addon:**

```bash
mvn clean package
```

Your addon JAR will be in `target/`.

---

## 🔄 Addon Lifecycle

Addons go through several states during their lifetime:

```
UNLOADED → LOADED → ENABLED → DISABLED → UNLOADED
    ↓         ↓         ↓          ↓
  (scan)  (onLoad) (onEnable) (onDisable)
```

### Lifecycle Methods

```kotlin
override fun onLoad() {
    // Called when addon is discovered and loaded
    // Initialize data structures, load config
    // NOT YET SAFE: Don't register events/commands here
}

override fun onEnable() {
    // Called when addon is enabled
    // Register listeners, commands, start tasks
    // SAFE: Full plugin API available
}

override fun onDisable() {
    // Called when addon is disabled/unloaded
    // Save data, cleanup resources
    // Commands are automatically unregistered
}

override fun onReload() {
    // Called when addon is reloaded
    // Reload configuration, refresh data
}
```

---

## 📚 API Reference

### AbstractAddon Class

Base class providing common functionality. Extend this for convenience.

#### Properties

```kotlin
protected val logger: Logger              // Logger for this addon
val dataFolder: File                      // Addon's data folder
protected val commandManager: CommandManager  // Command registration
var isEnabled: Boolean                    // Current enabled state
```

#### Methods

##### Command Registration
**Examples:**

```kotlin
// Simple command
registerCommand("mycommand") { sender, _, _, args ->
    sender.sendMessage("Hello!")
    true
}

// With tab completion
registerCommand(
    name = "setmode",
    aliases = listOf("gm", "mode"),
    tabCompleter = SimpleTabCompleter { _, _, _, args ->
        if (args.size == 1) {
            listOf("creative", "survival", "adventure")
        } else null
    }
) { sender, _, _, args ->
    // Handle command
    true
}
```

##### Event Registration

```kotlin
protected fun registerEvents(vararg listeners: Listener)
```

**Example:**

```kotlin
registerEvents(
    JoinListener(this),
    QuitListener(this),
    ChatListener(this)
)
```

##### Logging

```kotlin
protected fun logInfo(message: String)
protected fun logWarning(message: String)
protected fun logError(message: String, throwable: Throwable? = null)
```

**Example:**

```kotlin
logInfo("Addon started successfully")
logWarning("Configuration missing, using defaults")
logError("Failed to connect to database", exception)
```

### Addon Interface

Core interface that all addons must implement.

```kotlin
interface Addon {
    val id: String              // Unique identifier
    val name: String           // Display name
    val version: String        // Version number
    val authors: List<String>  // List of authors
    val description: String    // Brief description
    val dependencies: List<String>  // Required addon IDs
    val plugin: Plugin         // Main plugin instance

    fun onLoad()
    fun onEnable()
    fun onDisable()
    fun onReload()
}
```

---

## 📄 manifest.yml Format

The manifest file defines your addon's metadata.

### Required Fields

```yaml
name: AddonName              # Display name
version: 1.0.0              # Semantic version
main: com.example.MyAddon   # Full class path
author: YourName            # Primary author
description: Brief description of the addon
```

### Optional Fields

```yaml
authors:                    # Multiple authors
  - Author1
  - Author2

dependencies:               # Required addon IDs
  - other-addon-id
  - another-dependency

website: https://example.com  # Homepage URL
```

### Complete Example

```yaml
name: CoolAddon
version: 2.1.0
main: com.coolstudio.cooladdon.CoolAddon
author: CoolDev
description: Adds cool features to your server

authors:
  - CoolDev
  - ContributorName

dependencies:
  - core-addon
  - utilities-addon

website: https://github.com/cooldev/cooladdon
```

---

## 💡 Examples

### Example 1: Simple Command Addon

```kotlin
package com.example.hello

import org.crewco.swccore.api.addon.AbstractAddon
import org.bukkit.plugin.Plugin

class HelloAddon(plugin: Plugin) : AbstractAddon(plugin) {

    override val id = "hello"
    override val name = "Hello Addon"
    override val version = "1.0.0"
    override val authors = listOf("Dev")
    override val description = "Says hello"

    override fun onEnable() {
        super.onEnable()

        registerCommand("hello") { sender, _, _, args ->
            val target = args.firstOrNull() ?: sender.name
            sender.sendMessage("§aHello, $target!")
            true
        }
    }
}
```

### Example 2: Event Listener Addon

```kotlin
package com.example.welcome

import org.crewco.swccore.api.addon.AbstractAddon
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin

class WelcomeAddon(plugin: Plugin) : AbstractAddon(plugin), Listener {

    override val id = "welcome"
    override val name = "Welcome Addon"
    override val version = "1.0.0"
    override val authors = listOf("Dev")
    override val description = "Welcomes players"

    override fun onEnable() {
        super.onEnable()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("§aWelcome to the server!")
    }
}
```

### Example 3: Organized Addon

**Main Class:**
```kotlin
package com.example.organized

import org.crewco.swccore.api.addon.AbstractAddon
import org.bukkit.plugin.Plugin
import com.example.organized.commands.MainCommand
import com.example.organized.listeners.*

class OrganizedAddon(plugin: Plugin) : AbstractAddon(plugin) {

    override val id = "organized"
    override val name = "Organized Addon"
    override val version = "1.0.0"
    override val authors = listOf("Dev")
    override val description = "Well-organized addon"

    override fun onEnable() {
        super.onEnable()

        // Register listeners
        registerEvents(
            JoinListener(this),
            QuitListener(this),
            ChatListener(this)
        )

        // Register commands
        registerCommand(
            name = "organized",
            executor = MainCommand(this)
        )

        logInfo("All systems operational!")
    }
}
```

**Listener:**
```kotlin
package com.example.organized.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import com.example.organized.OrganizedAddon

class JoinListener(private val addon: OrganizedAddon) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        event.player.sendMessage("§aWelcome from ${addon.name}!")
        addon.logInfo("${event.player.name} joined")
    }
}
```

**Command:**
```kotlin
package com.example.organized.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import com.example.organized.OrganizedAddon

class MainCommand(private val addon: OrganizedAddon) : CommandExecutor {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        sender.sendMessage("§e${addon.name} v${addon.version}")
        return true
    }
}
```

---

## 🐛 Troubleshooting

### Addon Not Appearing in List

**Possible Causes:**
- Missing `manifest.yml` file
- Invalid YAML syntax in manifest
- Incorrect `main` class path

**Solution:**
```bash
# Check if manifest.yml exists in JAR
jar tf MyAddon.jar | grep manifest.yml

# View manifest contents
unzip -p MyAddon.jar manifest.yml

# Check server console for errors
/addon list
```

### Addon Failed to Load

**Common Errors:**

1. **"does not have a manifest.yml file"**
    - Ensure `manifest.yml` is in `src/main/resources/`
    - Rebuild your addon

2. **"manifest.yml does not have a 'main' entry"**
    - Add `main: com.example.MyAddon` to manifest.yml
    - Verify the class path is correct

3. **"does not implement Addon interface"**
    - Ensure your class extends `AbstractAddon` or implements `Addon`
   ```kotlin
   class MyAddon(plugin: Plugin) : AbstractAddon(plugin)
   ```

4. **"must have a constructor that takes Plugin as parameter"**
    - Add Plugin parameter to constructor:
   ```kotlin
   class MyAddon(plugin: Plugin) : AbstractAddon(plugin)
   ```

### Commands Not Working

**Checklist:**
- ✅ Called `super.onEnable()` first
- ✅ Used `registerCommand()` method
- ✅ Command returns `true` when handled
- ✅ No conflicting command names

**Debug:**
```kotlin
registerCommand("mycommand") { sender, _, _, _ ->
    logInfo("Command executed!")  // Check console
    sender.sendMessage("It works!")
    true  // Must return true
}
```

### Events Not Firing

**Checklist:**
- ✅ Class implements `Listener`
- ✅ Registered with `registerEvents()` or `pluginManager.registerEvents()`
- ✅ Method has `@EventHandler` annotation
- ✅ Event parameter type is correct
- ✅ Method is public (not private)

**Example:**
```kotlin
class MyAddon(plugin: Plugin) : AbstractAddon(plugin), Listener {

    override fun onEnable() {
        super.onEnable()
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler  // Don't forget this!
    fun onJoin(event: PlayerJoinEvent) {  // Must be public
        logInfo("Event fired!")
        event.player.sendMessage("Hello!")
    }
}
```

### Permission Denied

**Issue:** Players can't use `/addon` command

**Solution:**
```bash
# Grant permission
/lp user PlayerName permission set swccore.addon

# Or grant to group
/lp group admin permission set swccore.addon
```

### ClassNotFoundException

**Issue:** Addon can't find classes from SWCCore

**Solution:**
- Ensure `SWCCore.jar` is in your `libs/` folder during compilation
- Verify `<systemPath>` in pom.xml points to the correct location
- Rebuild your addon: `mvn clean package`

---

## 🔥 Hot Reload Workflow

Update an addon without restarting:

```bash
# 1. Disable the old version
/addon disable my-addon

# 2. Unload it from memory
/addon unload my-addon

# 3. Replace the JAR file in addons folder
# (Upload new MyAddon-1.1.0.jar)

# 4. Load the new version
/addon load MyAddon-1.1.0.jar

# 5. Enable it
/addon enable my-addon
```

---

## 📞 Support

- **Issues:** Report bugs or request features
- **Documentation:** Check the main SWCCore docs
- **Examples:** See the `examples/` folder in the repository

---

## 📄 License

[GNU-Public]

---

**Made with ❤️ by the SWCCore team**