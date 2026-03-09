# Wear & TV Modules

## Wear OS – Gumdrops

The `wear/` module should surface:

- **Notifications**: "New app available", "Trail completed", clan/shop updates.
- **Micro-interactions**: Compact "gumdrop" cards (small, tappable tiles) for quick actions (e.g. open catalog, claim daily reward).
- **Data**: Reuse catalog/notifications from main app via shared DataStore or Wear Data Layer; no new backend required.

## Android TV – Cinematic Sweets

The `tv/` module should include:

- **TV-friendly catalog**: Large tiles, D-pad navigation, no touch dependency.
- **Trailers**: Use `trailerUrl` from the app manifest; TV detail screen embeds a video player (e.g. ExoPlayer).
- **Manifest**: Reuse `docs/sugarmunch-apps.json`; ensure `trailerUrl` is set for apps that have video previews.

## Implementation notes

- Keep business logic in shared modules or replicate minimal logic in wear/tv.
- Sync state (e.g. theme, rewards) via Data Layer or a small sync API.
