# SugarMunch Automation System

A comprehensive automation/task system for SugarMunch that allows users to create custom automated workflows. Think "IFTTT for SugarMunch" or "iOS Shortcuts for Android".

## Overview

The Automation system enables users to create powerful, event-driven workflows that can:
- Trigger based on time, app events, system state, location, and sensors
- Execute effects, themes, app actions, and system commands
- Chain multiple actions with conditions
- Use pre-built templates or create custom automations

## Architecture

### Core Components

```
app/src/main/kotlin/com/sugarmunch/app/automation/
├── AutomationData.kt          # Room entities, DAO, Repository
├── AutomationTriggers.kt      # All trigger types & evaluation
├── AutomationActions.kt       # All action types & execution
├── AutomationEngine.kt        # Core automation engine
├── TaskBuilder.kt            # Visual task builder backend
├── TaskScheduler.kt          # WorkManager & AlarmManager integration
├── Templates.kt              # Pre-built automation templates
```

### UI Components

```
app/src/main/kotlin/com/sugarmunch/app/ui/screens/automation/
├── AutomationScreen.kt       # List of user automations
└── TaskBuilderScreen.kt      # Visual task builder UI
```

## Features

### 1. Triggers (15+ Types)

**Time-based:**
- Specific time (daily or specific days)
- Interval-based (every X minutes)
- Sunrise/sunset with offset

**App-based:**
- App opened/closed
- App installed

**Event-based:**
- Effect toggled
- Theme changed
- Reward claimed

**System-based:**
- Battery level (above/below threshold)
- Charging state (plugged/unplugged/fast/wireless)
- WiFi connected (specific or any)
- Bluetooth connected
- Screen state (on/off/unlocked)

**Location-based:**
- Geofence enter/exit/dwell

**Sensor-based:**
- Shake (with sensitivity)
- Orientation change
- Proximity

**Manual:**
- One-tap execution
- Shortcut support

### 2. Conditions (Optional)

- Time window (only during certain hours)
- Battery level range
- Charging state
- Location (inside/outside area)
- WiFi connection
- App running
- Effect active
- Composite (AND/OR/NOT logic)

### 3. Actions (20+ Types)

**Effect Actions:**
- Enable/disable/toggle effect
- Set effect intensity

**Theme Actions:**
- Change theme
- Random theme (by category)
- Set theme intensity (colors/background/particles/animations)

**App Actions:**
- Open app
- Launch SugarMunch screen
- Share app

**SugarMunch Actions:**
- Claim daily reward
- Add Sugar Points

**System Actions:**
- Show notification
- Show toast
- Vibrate (patterns)
- Set brightness
- Set volume
- Play sound
- Turn off screen

**Control Flow:**
- Wait/delay
- Conditional (if/else)
- Run another task

### 4. Pre-built Templates

1. **Focus Mode** - Work hours = calm theme + dim effects
2. **Gaming Mode** - Gaming apps = gaming preset + high energy
3. **Bedtime** - 10pm = night theme + all effects off
4. **Morning Boost** - 7am = sugar rush + daily reward
5. **Battery Saver** - <20% = minimal effects
6. **Workout Mode** - Shake = high energy + heartbeat
7. **Night Owl** - Sunset = darker theme
8. **Party Mode** - Weekend evenings = maximum everything
9. **Arrival Home** - Home WiFi = relaxing theme
10. **Screen Time Reward** - Usage time = bonus points

## Usage

### Creating an Automation

```kotlin
// Using the fluent TaskBuilder API
val task = TaskBuilder(context)
    .name("Morning Routine")
    .description("Start the day right")
    .atTime(hour = 7, minute = 0)
    .onlyOnWeekdays()
    .changeTheme("sugarrush_classic", intensity = 1.2f)
    .enableEffect("sugarrush")
    .claimDailyReward()
    .showToast("Good morning!")
    .buildAndSave()
```

### Using Templates

```kotlin
val templateManager = TemplateManager.getInstance(context)
templateManager.applyTemplate("template_morning_boost")
```

### Running Tasks

```kotlin
val engine = AutomationEngine.getInstance(context)

// Run immediately
engine.runTask(taskId)

// Schedule for later
engine.scheduleTask(taskId, triggerTime)

// Enable/disable
engine.setTaskEnabled(taskId, enabled)

// Delete
engine.deleteTask(taskId)
```

### Manual Trigger

```kotlin
// Emit manual trigger event
AutomationEventBus.emitManualTrigger("my_trigger_id")
```

## Database Schema

### Entities

```kotlin
// AutomationTaskEntity
- id: String (PK)
- name: String
- description: String
- enabled: Boolean
- triggerJson: String (serialized)
- conditionsJson: String (serialized)
- actionsJson: String (serialized)
- createdAt: Long
- lastRunAt: Long?
- runCount: Int
- isTemplate: Boolean
- templateId: String?
- variablesJson: String

// ExecutionHistoryEntity
- id: String (PK)
- taskId: String
- taskName: String
- triggerTime: Long
- completionTime: Long?
- status: String
- actionsExecuted: Int
- actionsTotal: Int
- errorMessage: String?
- triggeredBy: String
```

## Scheduling

### WorkManager (Background Tasks)
- Battery-friendly periodic checks
- Respects Doze mode
- Automatic retry on failure

### AlarmManager (Exact Timing)
- Precise time-based triggers
- Works with device reboot
- Wakes device if needed

### Geofencing
- Location-based triggers
- Enter/exit/dwell transitions
- Battery-optimized

## UI Screens

### AutomationScreen
- List of all user automations
- Enable/disable toggle
- Last run info & run count
- Quick run button
- Empty state with quick actions
- Stats overview (active count, total runs)

### TaskBuilderScreen
4-step visual builder:
1. **Choose Trigger** - Cards for each trigger type
2. **Set Conditions** - Optional filtering
3. **Add Actions** - Draggable list with reordering
4. **Review & Save** - Name and confirm

## Integration

### Navigation
Added to NavGraph:
- `/automation` - Automation list
- `/task-builder` - Create/edit tasks

### Settings
Added "Automation" card to SettingsScreen for easy access.

## Event Bus

The `AutomationEventBus` allows internal events to trigger automations:

```kotlin
// Emit events from anywhere
AutomationEventBus.emitEffectToggled(effectId, enabled)
AutomationEventBus.emitThemeChanged(themeId)
AutomationEventBus.emitRewardClaimed(rewardType)
AutomationEventBus.emitManualTrigger(triggerId)
```

## Permissions

Required permissions for full functionality:
- `RECEIVE_BOOT_COMPLETED` - Reschedule after reboot
- `ACCESS_FINE_LOCATION` - Geofencing triggers
- `ACCESS_BACKGROUND_LOCATION` - Background geofencing
- `SCHEDULE_EXACT_ALARM` - Exact time triggers (Android 12+)
- `VIBRATE` - Vibration actions
- `WRITE_SETTINGS` - Brightness control

## Future Enhancements

- Voice trigger integration
- NFC tag triggers
- Widget shortcuts
- Quick Settings tiles
- Import/export automations (JSON)
- Community template sharing
- Analytics dashboard
- Variable interpolation
- HTTP/webhook actions

## Files Created

### Backend (9 files, ~200KB)
1. `AutomationData.kt` - Data models, Room entities, Repository (25KB)
2. `AutomationTriggers.kt` - Trigger definitions & evaluator (42KB)
3. `AutomationActions.kt` - Action definitions & executor (30KB)
4. `AutomationEngine.kt` - Core engine (17KB)
5. `TaskBuilder.kt` - Builder API & UI state (33KB)
6. `TaskScheduler.kt` - Scheduling backend (19KB)
7. `Templates.kt` - Pre-built templates (29KB)

### UI (2 files, ~78KB)
8. `AutomationScreen.kt` - Automation list UI (24KB)
9. `TaskBuilderScreen.kt` - Visual builder UI (54KB)

### Modified Files
- `AppDatabase.kt` - Added automation entities & DAO
- `NavGraph.kt` - Added automation routes
- `SettingsScreen.kt` - Added automation card

Total: ~280KB of new code!
