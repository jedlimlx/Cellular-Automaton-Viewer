# Higher Range Outer Totalistic (HROT)

HROT rules are outer totalistic rules that make use of neighbourhoods with range larger than 1. <br>
CAViewer supports arbitrary neighbourhoods via the [CoordCA format](https://github.com/saka29/CoordCA#rule-format) (Specify with N@). <br>
CAViewer also supports arbitary weighted neighbourhoods & state weights via the [LifeViewer format](https://conwaylife.com/forums/viewtopic.php?f=3&t=1622&start=1677) (Specify with NW).<br>
In addition, weighted neighbourhoods can be specified in the neighbourhood selector in the HROT rule dialog. <br>

Rules on triangular and hexagonal tilings are also supported. 
Append H for hexagonal or T for triangular to the custom neighbourhood / weights specifier 
to get the rule on a hexagonal or triangular grid.

####  Supported / Planned Named HROT Neighbourhoods
- [x] Asterisk (A)
- [x] Checkerboard (B)
- [x] Aligned Checkerboard (b)
- [x] Circular (C)
- [ ] Triangular Neighbourhood on Triangular Grid (D)
- [x] Gaussian Weighted Neighbourhood (G)
- [x] Hexagonal (H)
- [x] Triangular Moore (L)
- [x] Moore (M)
- [x] Von Neumann (N)
- [ ] Triangular Neighbourhood on Hexagonal Grid (T)
- [x] Saltire (X)
- [x] Euclidean (2)
- [x] Tripod (3)
- [ ] Hexagram (6)
- [x] Star (*)
- [x] Cross (+)
- [x] Hash (#)
- [x] CoordCA Format (Custom Neighbourhoods @)
- [x] LifeViewer Format (Weighted Neighbourhoods W)
- [x] LifeViewer Format (State Weights W)

Use N@ with no extra hex digits if you specify the neighbourhood / state weights in the neighbourhood / state weights selector.

#### How do (weighted) neighbourhoods work?
Take this neighbourhood as an example:
```
0,0,1,0,0
0,1,1,1,0
1,1,x,1,1
0,1,1,1,0
0,0,1,0,0
```

If the cells around it are in this configuration:
```
0,0,1,0,1
1,0,1,0,0
0,1,x,1,0
0,1,1,1,0
0,0,0,0,1
```

Then, the cell has 7 neighbours as 7 cells coincide with the '1's on the neighbourhood.

Now what if I had this weighted neighbourhood and the same configuration:
```
0,0,-1,0,0
0,1,1,1,0
-1,1,x,1,-1
0,1,1,1,0
0,0,-1,0,0
```

Then the cell would have a neighbourhood sum of `6 * 1 + 1 * -1 = 5`.

For multi-state rules such as generations, state weights can also be used. <br>
The cell will be added to the neighbourhood sum with a value of `neighbourhood weight * state weight`. <br>

Alive cells in 2-state rules have a state weight of 1 and dead cells have a state weight of 0. <br>

#### Rule Format
Weights should be expressed in this format in RLE files as comments (ensure the use of #R)
```
#R 1 2 3 2 1
#R 2 4 6 4 2
#R 3 6 9 6 3
#R 2 4 6 4 2
#R 1 2 3 2 1
```

State weights are placed below neighbourhood weights and are written as follows. <br>
The first number represents the state weight of the 0th state and so on.
```
#R 0 1 0 0 2 -1 0 0
```

So a RLE file for a weighted rule should look like this:
```
#R 0 0 1 0 0 
#R 0 1 2 1 0 
#R 1 2 0 2 1 
#R 0 1 2 1 0 
#R 0 0 1 0 0
x = 2, y = 2, rule = R2,C2,S2-3,B4,7-8,N@
o$bo!
```

This only applies to pattern files. <br>
For neighbourhood weights in the rule dialog, use the neighbourhood weight selector or express weighted rules with `NW` and the [LifeViewer format](https://conwaylife.com/forums/viewtopic.php?f=3&t=1622&start=1677).

## 2 state
* Cells have 2 states, dead and alive. <br>
* Cells are born (turn from dead to alive) if they have X neighbours and X is in birth. <br>
* Cells stay alive if they have X neighbours and X is in survival. <br>
* If not, the cell dies (turn from alive to dead). <br>

B0 rules are supported via emulation by alternating rules. See [this](http://golly.sourceforge.net/Help/Algorithms/QuickLife.html) for information on the alternating rules are generated.

Apgtable generation is supported for normal rules, B0 rules and weighted rules. <br>
Note that the generated apgtables for weighted rules can be several gigabytes in size if you are using a large neighbourhood like R2 Moore.

The code that simulates HROT rules can be found [here](../src/main/java/sample/model/rules/hrot/HROT.java).

#### Rulestring format
R\<range\>,C2,S\<survival\>,B\<birth\>,N\<neighbourhood\> <br>
R\<range\>,C2,S\<survival\>,B\<birth\>,N@\<CoordCA\> <br>
R\<range\>,C2,S\<survival\>,B\<birth\>,NW\<Neighbourhood Weights\> <br>

## Generations
Generations rules are a multistate generalization of 2 state rules in which live cells can exist in different states, and cells that would die in a 2-state cellular automaton instead advance to the next state.

The name "Generations" is due to the conceptualisation of this process as cells "getting older" before eventually dying

* A dead cell
   * Will become alive if it has X neighbours and X is in birth.
   * If not, it stays dead.
* A living cell
   * Will stay alive if it has X neighbours and X is in survival.
   * If not, it advances to state 2 in the next generation of the pattern.
* A cell in state m ≥ 2 will advance to state ((m + 1) mod n) in the next generation of the pattern.

B0 rules are supported via emulation with alternating rules. See [this](./Rule-Support.md#b0-rules) for more information.

State weights can also be used with generations rules. By default, all states have a state weight of 0 except the alive state. <br>
This extension of generations rules is known as [Weighted Generations](http://www.mirekw.com/ca/rullex_wgen.html).

The code that simulates HROT Generations rules can be found [here](../src/main/java/sample/model/rules/hrot/HROTGenerations.java).

#### Rulestring format
R\<range\>,C\<states\>,S\<survival\>,B\<birth\>,N\<neighbourhood\> <br>
R\<range\>,C\<states\>,S\<survival\>,B\<birth\>,N@\<CoordCA\> <br>
R\<range\>,C\<states\>,S\<survival\>,B\<birth\>,NW\<Neighbourhood Weights\> <br>
R\<range\>,C\<states\>,S\<survival\>,B\<birth\>,NW\<Neighbourhood Weights\>,\<State Weights\> <br>

## Extended Generations
Extended Generations rules are a generalisation of Generations rules. 
This extension explores the result of changing the amounts of time cells can be active/inactive for, 
and also of allowing cells to "come back" from inactivity and alternate between being active/inactive an 
indefinite amount of times before dying.

* An active cell
    * Remains in its current state if it has X neighbours and X is in survival
    * If not, it advances to state ((x + 1) mod y) in the next generation of the pattern where x is the current state and y is the total number of states.
* An inactive cell
    * Advances to state ((x + 1) mod y) in the next generation of the pattern where x is the current state and y is the total number of states. 
      It behaves like a normal Generations dying cell.
* A dead cell
    Advances to state 1 if it has X neighbours and X is in birth.
* If not, it remains dead.

The code that simulates HROT Extended Generations rules can be found [here](../src/main/java/sample/model/rules/hrot/HROTExtendedGenerations.java).

#### Rulestring format
R\<range\>,B\<birth\>,S\<survival\>,G\<genext\>,N\<neighbourhood\> <br>
R\<range\>,B\<birth\>,S\<survival\>,G\<genext\>,N@\<CoordCA\> <br>
R\<range\>,B\<birth\>,S\<survival\>,G\<genext\>,NW\<Neighbourhood Weights\> <br>

## Deficient Rules
In deficient rules, after a cell is born with birth transition B, a dead cell in its neighbourhood 
cannot be born with the same birth transition. This deficiency will go away after the cell survives for a generation. 
If there is permanent deficiency, the cell will remain deficient forever until it dies.
This allows typically explosive rules such as B1 and B2 rules to be stable.

#### Rulestring format
R\<range\>,D\<permanentDeficiency?\>,S\<survival\>,B\<birth\>,N@\<CoordCA\> <br>
R\<range\>,D\<permanentDeficiency?\>,S\<survival\>,B\<birth\>,N\<neighbourhood\> <br>
R\<range\>,D\<permanentDeficiency?\>,S\<survival\>,B\<birth\>,NW\<Neighbourhood Weights\> <br>

The code that simulates HROT Integer rules can be found [here](../src/main/java/sample/model/rules/hrot/DeficientHROT.java).

## Integer Rules
An extension of a rule described by Mark Niemiec called Integer Life.

* At every step, a cell's neighborhood sum is the arithmetic sum of the values of its neighbors.
* If a cell is dead and has a neighborhood of exactly Xn and X is in the birth conditions, 
a new cell of value n is born there; otherwise it stays dead. If a cell can be born into mulitple states, the lowest is chosen.
* If a cell is alive (n > 0), and has exactly Xn neighbors and X is in the survival conditions, 
it remains alive; otherwise, it dies.
* Survival occurs in the range [Xn, (X+1)n)
* If a cell's state exceeds the maximum state, it dies.

#### Rulestring format
R\<range\>,I\<states\>,S\<survival\>,B\<birth\>,N\<neighbourhood\> <br>
R\<range\>,I\<states\>,S\<survival\>,B\<birth\>,N@\<CoordCA\> <br>
R\<range\>,I\<states\>,S\<survival\>,B\<birth\>,NW\<Neighbourhood Weights\> <br>

The code that simulates HROT Integer rules can be found [here](../src/main/java/sample/model/rules/hrot/IntegerHROT.java).

## Regenerating Generations
This is an extension of generations rules that allows dying to regenerating (ie. progress back to the alive state).

* A dead cell
    * Goes to the birth state if it has X neighbours and X is in birth
    * If not, it remains dead
* An alive cell
    * Remains alive if it has X neighbours and X is in survival
    * If not it goes to state 2
* A dying cell (state 2 and above)
    * Goes the state (n - 1) if it has X neighbours and X is in regen birth
    * Remains at the same state if it has X neighbours and X is in regen survival
    * Else, it progresses to the (n + 1) % m state

### Rulestring format
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,N\<neighbourhood\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,N@\<CoordCA\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,NW\<Neighbourhood Weights\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,NW\<Neighbourhood Weights\>,\<State Weights\> <br>

The code that simulates HROT Regenerating Generations can be found [here](../src/main/java/sample/model/rules/hrot/HROTRegeneratingGenerations.java).

# Isotropic Non-Totalistic (INT)
Support for INT rules is planned.

#### Supported / Planned INT Neighbourhoods
- [ ] Range 1 Moore Isotropic Non-Totalistic
- [ ] Range 1 Hexagonal Isotropic Non-Totalistic
- [ ] Range 2 Von Neumann Isotropic Non-Totalistic
- [ ] Range 2 Checkerboard Isotropic Non-Totalistic
- [ ] Range 2 Far Corners Isotropic Non-Totalistic
- [ ] Range 2 Far Edges Isotropic Non-Totalistic
- [ ] Range 2 Knight Life Isotropic Non-Totalistic
- [ ] Range 2 Cross Isotropic Non-Totalistic
- [ ] Range 3 Cross Isotropic Non-Totalistic


# 1D Cellular Automaton
CAViewer also supports 1D rules via the format used by Wolfram Alpha. This includes B0 rules.

The code that simulates 1D rules can be found [here](../src/main/java/sample/model/rules/misc/OneDimensional.java).

#### Rulestring format
W\<wolframNumber\> <br>
R\<range\>,C\<states\>,W\<wolframNumber\> <br>


# History Rules
History rules have 7 or more states. They are based on the HistoricalLife rule. See [here](https://conwaylife.com/wiki/OCA:LifeHistory) 
for more information.

History rules are (planned to be) supported by:
- [x] 2-state HROT
- [ ] HROT Generations
- [ ] HROT Extended Generations


# B0 rules
CAViewer makes use of a generalised B0 algorithm to emulate B0 rules.
Let's say the transition function is *c* = f(*x*, *n*) where *c* is the cell's next state, *x* is the cell's current state and *n* is the cell's neighbours.
To emulate a B0 rule, we need to ensure the background is 0 at all times. So let's say *B* is a list of background that the rules moves across.
The rule will alternate across *l* rules where *l* is the length of *B*.
Every generation, before inputting the cells into *f(x, n)*, we swap 0 and *B\[generation % l\]*. A similar function is applied to the output.

With that, we have successfully emulated a B0 rule!