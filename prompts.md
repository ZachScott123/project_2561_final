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

## Session 2 – 2026-06-18 16:10
Task: Task 2 (Observer pattern implementation part 2)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> Now show how to connect the GUI to those "DirectionControl" listeners so it updates on change and avoids polling the controls every timer tick.
Suggestion summary:
GitHub Copilot suggested registering `DirectionControl` listeners in `AircraftGUI`, updating GUI state on the Swing EDT using `SwingUtilities.invokeLater(...)`, and removing repeated `getCurrentValue()` polling from the timer update path. This part focused on wiring the model-to-view observer flow.
Decision: Accepted
Why: Connecting the GUI through listeners instead of polling creates a cleaner, event-driven update path and reduces unnecessary UI thread work.

## Session 2 – 2026-06-18 16:20
Task: Task 2 (Observer pattern implementation part 4)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> I need to make sure that my code does not call any swing method from directly inside of the listener.
Suggestion summary:
GitHub Copilot suggested removing all direct Swing calls from the listener callback and instead updating only thread-safe orientation state. The Swing Timer or EDT-based repaint path then reads that state and updates the display, keeping Swing interaction on the EDT.
Decision: Accepted
Why: The listener now only updates `volatile` state fields for roll/pitch/yaw, and the GUI update path remains responsible for calling Swing methods on the EDT.

## Session 2 – 2026-06-18 16:35
Task: Task 3 (Worker thread self-healing part 1)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> Now I am starting Task 3, my simulation currently is dependant on multiple background threads. If any of these threads throw exceptions, the thread dies in a silent manner. But the JVM continues to run. My question is how can I make this self healing, walk me through the first step of doing so. I will need to catch the exception and log it clearly with the worker's name and stack trace, wait shortly and then proceed to restart the work, rather than silently letting it die.
Suggestion summary:
GitHub Copilot suggested wrapping long-lived worker loops in a supervisor boundary that catches `Throwable`, logs the thread name and stack trace, sleeps briefly, and restarts the worker. This first step prevents silent thread death and makes failures visible.
Decision: Accepted
Why: Adding a supervisor loop around worker threads is the first step toward self-healing; it provides clear failure logs and automatic restart behavior.

## Session 2 – 2026-06-18 16:55
Task: Task 3 (Exponential backoff for worker restart)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> Now, I need you to help me make it so that it uses exponential backoff, meaning that I will start at 100ms, double after each failure (200, 400, 800, . . . ), and cap at 5 seconds. After a worker runs successfully for at least 10 consecutive seconds, it needs to reset the backoff to 100 ms.
Suggestion summary:
GitHub Copilot updated the turbulence worker supervisor to use exponential backoff for restarts, beginning at `100ms`, doubling after each failure, capping at `5000ms`, and resetting after `10s` of successful runtime.
Decision: Accepted
Why: This change makes the worker more resilient by avoiding rapid restart loops after repeated failures while restoring normal retry timing after sustained success.

## Session 2 – 2026-06-18 17:05
Task: Task 3 (Test failure injection guidance)
Tool: GitHub Copilot Chat
Prompt (verbatim):
> where can I add this, "throw new RuntimeException("test failure")"
Suggestion summary:
GitHub Copilot answered that the failure should be injected in `src/Main.java` inside `createTurbulenceThread(...)`, within the inner `while (running.get())` loop after the turbulence jitter is applied and before `Thread.sleep(200)`.
Decision: Accepted
Why: Placing the forced exception there simulates a worker failure during normal turbulence processing and validates the supervisor/backoff behavior.
