# SugarGallery 🍬

<div align="center">

![SugarGallery Banner](docs/images/banner.png)

**A candy-themed photo and video gallery app with extreme customization**

[![License: GPL-3.0](https://img.shields.io/badge/License-GPL3.0-pink.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-purple.svg)](https://kotlinlang.org/)
[![Minimum SDK](https://img.shields.io/badge/Min%20SDK-24-blue.svg)]()

[Features](#-features) • [Themes](#-sugar-themes) • [Installation](#-installation) • [Usage](#-usage) • [Customization](#-customization)

</div>

---

## 📱 About

SugarGallery is a privacy-focused photo and video gallery app forked from [Fossify Gallery](https://github.com/FossifyOrg/Gallery), transformed into the ultimate candy-themed experience with extreme customization options.

No ads. No unnecessary permissions. Just pure candy goodness! 🍭

---

## ✨ Features

### 🖼️ Core Gallery Features
- **Photo & Video Viewing** - Beautiful media grid with smooth scrolling
- **Multiple Format Support** - JPEG, JPEG XL, PNG, MP4, MKV, RAW, SVG, GIF, AVIF, WebP, and more
- **Media Editing** - Crop, resize, rotate, flip, draw, and apply filters
- **Privacy Protection** - Strip EXIF metadata (GPS, camera details) from photos
- **App Lock** - Pin, pattern, or fingerprint lock for specific items or entire app
- **Recycle Bin** - Recover deleted photos and videos within 30 days
- **Hidden Folders** - Exclude sensitive folders from view
- **Favorites** - Quick access to your favorite media
- **Album Management** - Create, rename, and organize albums

### 🍬 Sugar Extreme Features
- **30+ Candy Themes** - Cotton Candy, Chocolate, Gummy, Lollipop, Caramel, and more!
- **Extreme Sugar Rush** - Nuclear overload mode with 2.0x intensity
- **Candy Animations** - Sparkle, glow, float, shimmer, and more effects
- **Particle Effects** - Candy particles, sparkles, and magical trails
- **Customizable Intensity** - Adjust sugar intensity from 0.0 to 2.0
- **Candy Mode** - Toggle full candy experience on/off
- **Haptic Feedback** - Candy-themed vibration patterns
- **Candy FAB** - Floating action button with candy dispenser design

---

## 🎨 Sugar Themes

### Cotton Candy Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🍭 Cotton Candy | Fluffy pink and blue candy floss dreams | 1.2x |
| 💗 Pink Cotton Candy | Extra sweet pink candy floss | 1.5x |
| 💙 Blue Cotton Candy | Cool blue candy clouds | 1.3x |

### Chocolate Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🍫 Dark Chocolate | Rich dark chocolate with gold accents | 1.0x |
| 🥛 Milk Chocolate | Creamy milk chocolate sweetness | 1.1x |
| ⚪ White Chocolate | Ivory white with pink highlights | 1.2x |
| 💎 Ruby Chocolate | Rare pink-brown ruby chocolate | 1.4x |

### Gummy Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🐻 Gummy Bear | Translucent multicolor gummy bears | 1.6x |
| 🪱 Gummy Worm | Rainbow gradient gummy worms | 1.7x |
| 🍋 Sour Gummy | Neon sour candy explosion | 2.0x |

### Lollipop Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🍭 Lollipop Swirl | Spiral rainbow lollipop | 1.5x |
| 🍭 Chupa Chups | Classic red-white striped lollipop | 1.3x |
| 🌈 Rainbow Pop | Bright rainbow popsicle | 1.8x |

### Caramel Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🍯 Salted Caramel | Golden caramel with sea salt sparkle | 1.2x |
| 🌀 Caramel Delight | Swirled caramel gradient paradise | 1.4x |
| 🧈 Butterscotch | Golden yellow butterscotch warmth | 1.3x |

### Extreme Sugar Rush Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| ☢️ Nuclear Sugar | Neon green-yellow radioactive overload | 2.0x |
| 💥 Candy Overload | Maximum saturation candy explosion | 2.0x |
| 🌪️ Skittles Storm | Rainbow explosion candy storm | 1.9x |

### Seasonal Special Collection
| Theme | Description | Intensity |
|-------|-------------|-----------|
| 🎄 Candy Cane | Classic red-white striped candy cane | 1.4x |
| 🌿 Peppermint | Cool minty peppermint freshness | 1.2x |
| 🍪 Gingerbread | Warm gingerbread cookie sweetness | 1.1x |

---

## 📥 Installation

### Requirements
- Android 7.0 (API level 24) or higher
- 100 MB of free storage space
- Storage permission for accessing media

### From Source
```bash
# Clone the repository
git clone https://github.com/SugarMunch/SugarGallery.git
cd SugarGallery

# Build debug APK
./gradlew assembleFossDebug

# Install on device
./gradlew installFossDebug
```

### From APK
1. Download the latest APK from [Releases](https://github.com/SugarMunch/SugarGallery/releases)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK file
4. Follow the installation prompts

---

## 🚀 Usage

### First Launch
1. Open SugarGallery
2. Grant storage permission when prompted
3. Complete the Candy Factory onboarding (7 screens)
4. Choose your initial sugar theme
5. Start browsing your media!

### Basic Navigation
- **Bottom Navigation** - Switch between All Media, Photos, Videos, Favorites, and Folders
- **Toolbar** - Access search, theme picker, sort options, and settings
- **FAB** - Quick add/camera button with candy animation
- **Long Press** - Select multiple items for batch operations

### Media Operations
- **Tap** - Open media in full-screen viewer
- **Double Tap** - Toggle favorite
- **Pinch** - Zoom in/out (photos)
- **Swipe** - Navigate between media
- **Long Press** - Select and access context menu

---

## ⚙️ Customization

### Display Settings
- **Grid Column Count** - Adjust from 2 to 6 columns
- **Show Media Details** - Toggle filename and date display
- **Thumbnail Quality** - Balance between quality and performance (50–100%)
- **Font Size** - Adjust text size (0.8x–1.5x)

### Theme Settings
- **Sugar Theme** - Select from 30+ candy themes
- **Candy Mode** - Enable/disable full candy experience
- **Sugar Intensity** - Adjust effect intensity (0.0–2.0)

### Animation Settings
- **Enable Animations** - Toggle all animations
- **Animation Speed** - Adjust speed (0.5x–2.0x)
- **Particle Effects** - Enable/disable candy particles
- **Candy Animations** - Special candy-themed animations

### Privacy Settings
- **App Lock** - Secure with PIN, pattern, or fingerprint
- **Hidden Folders** - Exclude folders from view
- **Strip EXIF** - Remove metadata when sharing

### Recycle Bin Settings
- **Enable Recycle Bin** - Toggle deleted item recovery
- **Retention Period** - Keep deleted items 1–90 days
- **Clear Recycle Bin** - Permanently delete all items

### Editor Settings
- **Default Filter** - Set default candy filter
- **Auto-Save Edits** - Automatically save edited photos
- **Edit Quality** - Output quality for edits (50–100%)

---

## 🎨 Candy Effects

SugarGallery includes 30+ unique candy effects:

| Effect | Description |
|--------|-------------|
| ✨ Sparkle | Twinkling sparkle particles |
| 💫 Glow | Pulsing glow effect |
| 🎈 Float | Floating animation |
| 💎 Shimmer | Shimmering surface |
| 🔲 Emboss | Embossed 3D effect |
| 🌊 Smooth | Smooth gradient transitions |
| 🔥 Warm | Warm color temperature |
| ☁️ Soft | Soft, fluffy appearance |
| 💎 Ruby Glow | Special ruby glow effect |
| 🍮 Jelly | Jelly-like wobble |
| 📳 Wobble | Wobbly deformation |
| 🔍 Translucent | See-through effect |
| 🌈 Rainbow | Rainbow gradient |
| ⚡ Neon | Neon glow |
| ⚡ Electric | Electric spark effect |
| 🌀 Swirl | Spiral swirl pattern |
| 🔄 Spin | Rotating animation |
| 📏 Stripes | Striped pattern |
| 🎯 Classic | Classic candy style |
| 💥 Pop | Popping animation |
| 🌟 Bright | Extra brightness |
| 🏆 Golden | Golden shimmer |
| ☢️ Nuclear | Radioactive glow |
| 🚀 Overload | Maximum intensity |
| 🎆 Explosion | Burst effect |
| 🌪️ Storm | Particle storm |
| ⭐ Particles | Particle effects |
| 🎉 Festive | Celebration effect |
| ❄️ Cool | Cool color temperature |
| 🌿 Fresh | Fresh appearance |
| 🏠 Cozy | Warm, cozy feeling |

---

## 🔒 Privacy

SugarGallery respects your privacy:

- **No Internet Permission** - The app doesn't connect to the internet
- **No Analytics** - No tracking or data collection
- **No Ads** - Completely ad-free
- **Local Storage Only** - All data stays on your device
- **EXIF Stripping** - Remove location and camera data from photos
- **App Lock** - Protect sensitive media with biometric or PIN lock
- **Hidden Folders** - Exclude private folders from view

---

## 🛠️ Development

### Tech Stack
- **Language** - Kotlin
- **Min SDK** - 24 (Android 7.0)
- **Target SDK** - 34 (Android 14)
- **UI Framework** - Material Design 3
- **Architecture** - MVVM with Repository pattern
- **Database** - Room Persistence Library
- **Image Loading** - Glide, Picasso
- **Media Processing** - Android Image Cropper, ExifInterface

### Project Structure
```
SugarGallery/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/sugarmunch/gallery/
│   │   │   ├── ui/
│   │   │   │   ├── activities/      # Main activities
│   │   │   │   ├── fragments/       # UI fragments
│   │   │   │   ├── adapters/        # RecyclerView adapters
│   │   │   │   └── dialogs/         # Custom dialogs
│   │   │   ├── data/
│   │   │   │   ├── repositories/    # Data repositories
│   │   │   │   ├── models/          # Data models
│   │   │   │   ├── dao/             # Room DAOs
│   │   │   │   └── entity/          # Room entities
│   │   │   ├── helpers/             # Helper classes
│   │   │   ├── extensions/          # Kotlin extensions
│   │   │   └── interfaces/          # Interfaces
│   │   └── res/
│   │       ├── layout/              # XML layouts
│   │       ├── drawable/            # Drawable resources
│   │       ├── values/              # Colors, strings, themes
│   │       ├── menu/                # Menu resources
│   │       └── anim/                # Animations
│   └── build.gradle.kts
└── build.gradle.kts
```

### Building
```bash
# Debug build
./gradlew assembleFossDebug

# Release build
./gradlew assembleFossRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

---

## 📸 Screenshots

<div align="center">

![Screenshot 1](docs/images/screenshot1.png)
![Screenshot 2](docs/images/screenshot2.png)
![Screenshot 3](docs/images/screenshot3.png)
![Screenshot 4](docs/images/screenshot4.png)

</div>

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add comments for complex logic
- Write tests for new features

---

## 📄 License

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

This app is a fork of [Fossify Gallery](https://github.com/FossifyOrg/Gallery), which is licensed under GPL-3.0.

---

## 🙏 Acknowledgments

- [Fossify Gallery](https://github.com/FossifyOrg/Gallery) - Original gallery app
- [Fossify Commons](https://github.com/FossifyOrg/Commons) - Shared utilities
- [Material Design 3](https://m3.material.io/) - Design system
- [Glide](https://github.com/bumptech/glide) - Image loading library
- [Picasso](https://square.github.io/picasso/) - Image downloading library

---

## 📞 Support

- **Issues** - [GitHub Issues](https://github.com/SugarMunch/SugarGallery/issues)
- **Discussions** - [GitHub Discussions](https://github.com/SugarMunch/SugarGallery/discussions)
- **Email** - support@sugarmunch.app

---

## 🎯 Roadmap

- [ ] Add more candy themes (user requests)
- [ ] Implement custom theme creator
- [ ] Add candy-themed widgets
- [ ] Implement cloud sync (optional)
- [ ] Add AI-powered photo organization
- [ ] Implement facial recognition (optional, on-device)
- [ ] Add more candy filters
- [ ] Implement slideshow with candy transitions

---

<div align="center">

**Made with 🍬 and ❤️ by the SugarMunch Team**

⭐ Star this repo if you like it!

</div>
