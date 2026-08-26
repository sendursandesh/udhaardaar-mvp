# V3 UI Keyboard and Safe-Area Fix

Requirements for V3 UI implementation:

- Use edge-to-edge/insets correctly so app content does not overlap the status bar or Android navigation/gesture area.
- Use IME-aware scrolling/resizing so the focused input is automatically brought above the keyboard.
- Keep bottom action controls above both the IME and navigation bar when visible.
- Apply consistently to all relevant V3 forms/screens.
- Do not require the user to press Back before a lower field becomes visible.
- Preserve the professional V3 visual design.

Acceptance test: focus every editable field from top to bottom with the keyboard open; each focused field must remain visible, and the final field plus bottom action controls must remain above Android system controls.