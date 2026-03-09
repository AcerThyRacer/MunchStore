# Accessibility (a11y)

## Summary

SugarMunch uses Compose with content descriptions and semantic properties for TalkBack. Key areas:

- **Navigation:** All bottom nav and main actions use `contentDescription` (see `values/strings.xml` `a11y_*`).
- **Effects / Theme:** Toggles and sliders should have `Modifier.semantics { contentDescription = getString(...) }` or `contentDescription` on the control.
- **Catalog:** App cards and list items need meaningful descriptions (e.g. app name + “Download” or “Open”).

## Checklist

- [ ] Run TalkBack on Catalog, Settings, Effects, Theme, Shop, Rewards.
- [ ] Ensure focus order is logical (top-to-bottom, then left-to-right).
- [ ] Check touch targets ≥ 48dp where possible.
- [ ] Verify contrast (text/background) meets WCAG AA where applicable.
- [ ] Reduce-motion: respect `Settings.Global.ANIMATOR_DURATION_SCALE` or user preference for reduced motion.

## References

- `app/src/main/res/values/strings.xml` – `a11y_*` strings for content descriptions
- [Compose accessibility](https://developer.android.com/jetpack/compose/accessibility)
