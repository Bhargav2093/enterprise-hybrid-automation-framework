"""Renders a terminal-style demo GIF of a real `mvn test -Pselfhealing-benchmark` run.

Not a screen recording (headless Chrome/a real pty isn't available in every environment this
might run in) - it's a scripted, deterministic PIL rendering of a REAL captured transcript: the
WARN log lines below are copied verbatim from an actual run (see
src/test/java/.../selfhealing/SelfHealingBenchmarkTest.java), and the JSON at the end is the real
committed samples/selfhealing-benchmark/benchmark-summary.json. Re-running the command shown
reproduces equivalent output (exact line order/timestamps will differ, values won't).

Requires Pillow: pip install pillow
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

_FONT_PATH = "C:/Windows/Fonts/consola.ttf"
_FONT_SIZE = 15
_LINE_HEIGHT = 20
_PAD_X = 18
_PAD_Y = 16
_BG = (30, 30, 30)
_FG = (212, 212, 212)
_PROMPT = (78, 201, 176)
_SUCCESS = (106, 200, 106)
_WARN = (229, 192, 92)
_MUTED = (140, 140, 140)

_COMMAND = "mvn test -Pselfhealing-benchmark"

# Real WARN lines copied verbatim from an actual `mvn test -Pselfhealing-benchmark` run (a
# representative subset of all 70 - the full set is in samples/selfhealing-benchmark/).
_HEALING_LINES = [
    "WARN  SelfHealingLocator - Self-healed homePage.productsNavLink using fallback candidate "
    "#1 (css=a[href^='/products'])",
    "WARN  SelfHealingLocator - Self-healed loginSignupPage.loginButton using fallback candidate "
    "#1 (xpath=//button[contains(text(),'Login')])",
    "WARN  SelfHealingLocator - Self-healed accountInfoPage.firstNameInput using fallback "
    "candidate #1 (css=input[data-qa='first_name'])",
    "WARN  SelfHealingLocator - Self-healed productDetailsPage.quantityInput using fallback "
    "candidate #1 (css=input#quantity)",
    "WARN  SelfHealingLocator - Self-healed checkoutPage.placeOrderLink using fallback candidate "
    "#1 (css=a.check_out)",
    "WARN  SelfHealingLocator - Self-healed contactUsPage.submitButton using fallback candidate "
    "#1 (id=submit)",
]

_SUMMARY_JSON = [
    "{",
    '  "pagesDefined" : 12,',
    '  "elementsDefined" : 70,',
    '  "elementsWithFallback" : 70,',
    '  "elementsWithFallbackPercent" : 100.0,',
    '  "singleCandidateElements" : 0,',
    '  "averageCandidatesPerElement" : 2.11,',
    '  "healingEventsProvenInThisRun" : 70',
    "}",
]

_OUTPUT_LINES: list[tuple[str, tuple[int, int, int]]] = [("", _FG)]
_OUTPUT_LINES += [(line, _WARN) for line in _HEALING_LINES]
_OUTPUT_LINES += [("... 64 more (see samples/selfhealing-benchmark/self-healing-report.json)", _MUTED)]
_OUTPUT_LINES += [("", _FG)]
_OUTPUT_LINES += [("Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- in TestSuite", _SUCCESS)]
_OUTPUT_LINES += [("", _FG)]
_OUTPUT_LINES += [("BUILD SUCCESS", _SUCCESS)]
_OUTPUT_LINES += [("", _FG)]

_CANVAS_W = 1150
_TYPE_CHARS_PER_FRAME = 3
_TYPE_FRAME_MS = 35
_OUTPUT_FRAME_MS = 130
_EMPHASIS_FRAME_MS = 320
_FINAL_HOLD_MS = 3200


def _font() -> ImageFont.FreeTypeFont:
    return ImageFont.truetype(_FONT_PATH, _FONT_SIZE)


def render() -> None:
    font = _font()
    frames: list[Image.Image] = []
    durations: list[int] = []

    def add_frame(lines: list[tuple[str, tuple[int, int, int]]], duration_ms: int, cursor: bool = False) -> None:
        height = _PAD_Y * 2 + max(1, len(lines)) * _LINE_HEIGHT
        img = Image.new("RGB", (_CANVAS_W, height), _BG)
        draw = ImageDraw.Draw(img)
        y = _PAD_Y
        for text, color in lines:
            draw.text((_PAD_X, y), text, font=font, fill=color)
            y += _LINE_HEIGHT
        if cursor:
            last_text = lines[-1][0] if lines else ""
            cursor_x = _PAD_X + draw.textlength(last_text, font=font)
            cursor_y = y - _LINE_HEIGHT
            draw.rectangle([cursor_x, cursor_y + 2, cursor_x + 9, cursor_y + _LINE_HEIGHT - 2], fill=_FG)
        frames.append(img)
        durations.append(duration_ms)

    for n in range(0, len(_COMMAND) + 1, _TYPE_CHARS_PER_FRAME):
        add_frame([(f"$ {_COMMAND[:n]}", _FG)], _TYPE_FRAME_MS, cursor=True)

    command_line = [(f"$ {_COMMAND}", _FG)]
    add_frame(command_line, 500, cursor=True)
    add_frame(command_line, 500, cursor=False)

    revealed = list(command_line)
    for text, color in _OUTPUT_LINES:
        revealed = revealed + [(text, color)]
        emphasis = text.startswith("Tests run") or text == "BUILD SUCCESS"
        add_frame(revealed, _EMPHASIS_FRAME_MS if emphasis else _OUTPUT_FRAME_MS)

    cat_command = revealed + [("$ cat samples/selfhealing-benchmark/benchmark-summary.json", _PROMPT)]
    add_frame(cat_command, 700, cursor=True)

    for line in _SUMMARY_JSON:
        cat_command = cat_command + [(line, _FG)]
        add_frame(cat_command, _OUTPUT_FRAME_MS)

    final = cat_command + [("", _FG), ("$ ", _PROMPT)]
    add_frame(final, _FINAL_HOLD_MS, cursor=True)

    max_h = max(f.height for f in frames)
    padded = []
    for f in frames:
        if f.height == max_h:
            padded.append(f)
        else:
            canvas = Image.new("RGB", (_CANVAS_W, max_h), _BG)
            canvas.paste(f, (0, 0))
            padded.append(canvas)

    out_dir = Path(__file__).resolve().parent.parent / "samples" / "selfhealing-benchmark"
    out_dir.mkdir(parents=True, exist_ok=True)
    out_path = out_dir / "demo.gif"
    padded[0].save(
        out_path,
        save_all=True,
        append_images=padded[1:],
        duration=durations,
        loop=0,
        optimize=True,
    )
    print(f"Wrote {out_path} ({out_path.stat().st_size / 1024:.0f} KB)")


if __name__ == "__main__":
    render()
