## Session 1 – 2026-06-15 16:23
Task: Task 1 (Maneuvers CSV extraction part 1)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> I need to put the maneuvers found in createAutomatedDemoThread into a file named maneuvers.csv. How should I start extracting those maneuvers from the existing thread implementation and structure the data file?
Suggestion summary:
GitHub Copilot suggested identifying the maneuver data in `createAutomatedDemoThread`, then designing a CSV schema with columns for time, roll, pitch, and yaw. The recommendation was to keep the file small and easy to parse, starting with a header row and one row per maneuver step.
Decision: Accepted
Why: Breaking the task into a data extraction step first makes it easier to move from hard-coded maneuvers to a reusable CSV-driven format.

## Session 1 – 2026-06-15 17:12
Task: Task 1 (Maneuvers CSV extraction part 2)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> I need the file to contain "ManeuverScript" which wraps a .load() function to initialize the data. How do I set up the CSV loader and integrate it with 'ManeuverScript.load()' in the project?
Suggestion summary:
GitHub Copilot suggested creating `maneuvers.csv` with a header and maneuver rows, then updating `Main` to call `ManeuverScript.load(new File("maneuvers.csv"))` during startup. It also recommended ensuring `ManeuverScript` supports CSV parsing and that the new file is returned from the existing `.load()` method.
Decision: Accepted
Why: Using `ManeuverScript.load()` keeps the new data-driven maneuver source consistent with the current project architecture and centralizes file initialization.

## Session 2 – 2026-06-18 15:54
Task: Task 2 (Observer pattern implementation part 1)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> I need help implementing an Observer pattern to this Java aircraft simulation project. Start by showing me, in a step-by-step way of how to make "DirectionControl" publish value changes to listeners rather than having the GUI poll it.
Suggestion summary:
GitHub Copilot suggested adding a custom `DirectionControlListener` interface, using a thread-safe listener list, and notifying listeners when `currentValue` changes. The first part focused on refactoring `DirectionControl` to support observers.
Decision: Accepted
Why: Splitting the work makes the refactor safer by isolating the model-side observer support first.
