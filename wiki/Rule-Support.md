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
a new cell of value n is born there; otherwise it stays dead. If a cell can be born into mulitple states, the highest is chosen.
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

#### Rulestring format
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,N\<neighbourhood\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,N@\<CoordCA\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,NW\<Neighbourhood Weights\> <br>
R\<range\>,G\<states\>,L\<birthState\>,B\<birth\>,S\<survival\>,B\<regenBirth\>,S\<regenSurvival\>,NW\<Neighbourhood Weights\>,\<State Weights\> <br>

The code that simulates HROT Regenerating Generations can be found [here](../src/main/java/sample/model/rules/hrot/HROTRegeneratingGenerations.java).

## Multi-state Cyclic Rules
This is a rule family thought of by Yoel. There are plans to support his [notation](https://github.com/yoelmatveyev/Fireworld/blob/master/Multistate_cyclical_CA.md).
In this rule family, rules are state-symmetric. 
When all state 1s are replaced by state 2s and state 2s are replaced by state 3s and so on, the pattern should evolve in the same way.

C represents the number of states (including ground 0).

Each transition is a group of numbers that is the same length as the number of states (excluding ground 0). These represent the count of state-1 cells, then state-2, then state-3... Of course, these transitions are cyclical.

The B/M/S describes birth, mutation (alive -> other alive) and survival conditions for every state.
l is a special character meaning "all", so for exemple Bl2 = B02122232425262, Ml-31 = M01112141516171 and S2l-4-534 = S202122232634 (note that the extra minus signs are necessary to prevent ambiguity).

For a 3 state rule, there is 1 mutation set.
For a 4-state rule, there are 2 mutation sets and so on.

1st set of mutation transitions (represent transition to state 2)
2nd set of mutation transitions (represent transition to state 3)
and so on...

All transitions are expressed from the perspective of state 1 but are applied cyclically to the other states.

For HROT rules, the numbers are separated by commas (e.g. B0,3,l,l-0)

#### Rulestring format
B\<birth\>/M\<mutate\>/M\<mutate2\>/M.../S\<survival\>/C\<states\>
R\<range\>,C\<states\>,B\<birth\>,M\<mutate\>,M\<mutate2\>,...,S\<survival\>,N\<neighbourhood\>


# Isotropic Non-Totalistic (INT)
In INT rules, the transitions take into account not only the total number of live neighbors of a cell, 
but also the relative configuration of those neighbours. 
[See](https://conwaylife.com/wiki/Isotropic_non-totalistic_Life-like_cellular_automaton) for more information.

#### Supported / Planned INT Neighbourhoods
- [x] Range 1 Moore Isotropic Non-Totalistic (M)
- [ ] Range 1 Hexagonal Isotropic Non-Totalistic (H)
- [ ] Range 2 Von Neumann Isotropic Non-Totalistic (V2)
- [ ] Range 2 Checkerboard Isotropic Non-Totalistic (B2)
- [x] Range 2 Far Corners Isotropic Non-Totalistic (FC)
- [x] Range 2 Knight Life Isotropic Non-Totalistic (K)
- [x] Range 2 Cross Isotropic Non-Totalistic (C2)
- [ ] Range 3 Cross Isotropic Non-Totalistic (C3)
- [x] Range 3 Far Edges Isotropic Non-Totalistic (FE)


## 2 state <!-- 1 -->
* Cells have 2 states, dead and alive. <br>
* Cells are born (turn from dead to alive) if their neighbourhood configuration is in birth. <br>
* Cells stay alive  if their neighbourhood configuration is in birth. <br>
* If not, the cell dies (turn from alive to dead). <br>

B0 rules are supported via emulation by alternating rules. 

Apgtable generation is supported all rules. <br>

The code that simulates 2-state INT rules can be found [here](../src/main/java/sample/model/rules/isotropic/rules/INT.java).

#### Rulestring format
B\<birth\>/S\<survival\>/N\<neighbourhood\>

## Generations <!-- 1 -->
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

Apgtable generation is supported all rules. <br>

The code that simulates generations INT rules can be found [here](../src/main/java/sample/model/rules/isotropic/rules/INTGenerations.java).

#### Rulestring format
B\<birth\>/S\<survival\>/G\<states\>/N<neighbourhood\>
\<survival\>/\<birth\>/\<states\>/\<neighbourhood\>


# 1D Cellular Automaton
CAViewer also supports 1D rules via the format used by Wolfram Alpha. This includes B0 rules.

The code that simulates 1D rules can be found [here](../src/main/java/sample/model/rules/misc/OneDimensional.java).

#### Rulestring format
W\<wolframNumber\> <br>
R\<range\>,C\<states\>,W\<wolframNumber\> <br>


# History Rules
History rules have 7 or more states. They are based on the HistoricalLife rule and use the notation 
[R]History where [R] is the rest of the rulestring.
See [here](https://conwaylife.com/wiki/OCA:LifeHistory) for more information.

History rules are (planned to be) supported by:
- [x] 2-state HROT
- [ ] HROT Generations
- [x] 2-state INT
- [ ] INT Generations


# Symbiosis Rules
Symbiosis rules are an extension of regular 2-state rules. 
In Symbiosis rules, opposite states will stabilise each other.
B0 symbiosis rules are undefined. Similar to history rules, the notation is [R]Symbiosis.

Symbiosis rules are (planned to be) supported by:
- [x] 2-state HROT
- [ ] HROT Generations
- [ ] 2-state INT
- [ ] INT Generations


# DeadlyEnemies Rules
DeadlyEnemies rules are an extension of regular 2-state rules. 
In DeadlyEnemies rules, opposite states will kill each other.
B0 deadly enemies rules are undefined. Similar to history rules, the notation is [R]DeadlyEnemies.

DeadlyEnemies rules are (planned to be) supported by:
- [x] 2-state HROT
- [ ] HROT Generations
- [ ] 2-state INT
- [ ] INT Generations


# Energetic Rules
Energetic rules are an extension of regular 2-state rules by KittyTac and WildMyron 
where there is matter and anti-matter with on contact form energy cells (i.e. annihiliate).

Energy cells will produce photons in all directions. When 2 or more photons collide, a vacuum is produced.
When a photons collides with a matter / anti-matter cell, it turns into that cell.

Similar to history rules, the notation is [R]Energetic. 

Energetic Rules are (planned to be) supported by:
- [ ] 2-state HROT
- [ ] HROT Generations
- [x] 2-state INT
- [ ] INT Generations



# Custom Rules
For a list of neighbourhood aliases, see [this](../src/main/resources/ruleloader/neighbourhoods.txt)
Note that when CAViewer finds multiple @TABLE, @TREE and @SQC directives in a rulefile, 
CAViewer will alternate between them.

Alternating B0 rules are also supported.

### @RULE
The first line of a .rule file must start with @RULE followed by a space and then the rule name. 
For example:
```
@RULE WireWorld
```

The supplied rule name should match the name of the .rule file.

### @TABLE
This section is optional. it contains a transition table that can be loaded by the RuleLoader algorithm. 
This is a simple example:

```
# Signals (2/3) pass alongside a wire (1):
n_states:4
neighborhood:vonNeumann
symmetries:rotate4
var a={2,3}
var b={2,3}
var c={2,3}
a,0,b,1,c,b
```

Empty lines and anything following the hash symbol "#" are ignored. The following descriptors should appear before content:

n_states: specifies the number of states in the CA (from 0 to n_states-1 inclusive). <br>
neighborhood: specifies the cell neighborhood for the CA update step. Must be one of: vonNeumann, Moore, hexagonal, oneDimensional or list of coordinates representing the neighbourhood (unlike lifelib, the first and last coordinate need not be (0, 0)) <br>
symmetries: can be none, permute or one of the symmetries supported for the neighborhood you have chosen. <br>
(optional) tiling: can be square, hexagonal or triangular (neighbourhood should point down)

After the descriptors comes the variables and transitions. Each variable line should follow the form given in the above example to list the states. Variables should appear before the first transition that uses them.

Transition lines should have states or variables separated by commas. Only one transition (or variable) should appear on each line.

There are 2 types of variables - bounded and unbounded. <br>
A bounded variable starts with `var` like this `var a = {1, 2, 3}` <br>
An unbounded variable starts with `unbound` like this `unbound a = {1, 2, 3}` <br>

Bounded variables must have the same value throughout a transition line. 
Unbounded variables can take on any of the possible values in a transition line, not just one.

Work is being done to allow CAViewer to switch neighbourhoods and symmetries halfway through the @RULE directive.

Rule tables usually don't specify every possible set of inputs. For those not listed, the central cell remains unchanged.

Transition rules are checked in the order given — the first rule that matches is applied. If you want, you can write rules in the form of general cases and exceptions, as long as the exceptions appear first.

### @TREE
This section is optional. If present, it contains a rule tree that can be loaded by the RuleLoader algorithm. (If the .rule file also contains a @TABLE section, RuleLoader will use the first one it finds.) The contents of this section is identical to the contents of a .tree file.

Essentially, the tree format allows you to add your own rules to CAViewer without needing to know how to recompile CAViewer and 
without dealing with the intricacies of external libraries; it generates relatively compact files, 
and the data structure is designed for very fast execution.

A rule tree is nothing more than a complete transition table for a rule, expressed in a compressed, canonicalized tree format. For an n state rule, each tree node has n children; each child is either another tree node or a next state value. 
To look up a function of m variables, each of which is a state value, you start at the root node and select the child node corresponding to the value of the first variable. From that node, you select the child node corresponding to the value of the second variable, and so on. When you finally look up the value of the final variable in the last node, the result value is the actual next state value, rather than another node.

The tree format has fixed the order of variables used for these lookups. You may specify arbitary neighbourhoods as a list of cells appended after `num_neighbours=`. Unlike lifelib, the last 2 entries need not be (0, 0).

The header consists of comments (lines starting with a "#") that are ignored, and three required parameter values that must be defined before the first tree node. These values are defined, one per line, starting with the parameter name, then an equals sign, and finally an integer value. The three parameters are num_states, which must be in the range 2..256 inclusive, num_neighbours, which may be 4, 6, 8 or a list of cell coordinates representing an arbitary neighbourhood, and num_nodes, which must match the number of node lines. tiling can also be added to specify square, hexagonal or triangular tilings.

The tree is represented by a sequence of node lines. Each node line consists of exactly num_states+1 integers separated by single spaces. The first integer of each node line is the depth of that node, which must range from 1..num_neighbors+1. The remaining integers for nodes of depth one are state values. The remaining integers for nodes of depth greater than one are node numbers. Nodes are numbered in the order they appear in the file, starting with zero; each node must be defined before its node number is used. The root node, which must be the single node at depth num_neighbors+1, must be the last node defined in the file.

### @SQC
This represents a Square Cell ruletable.
Essentially, it is a large transition table specifying the new state of a cell and the necessary neighbourhood sym.

Let's say I have the following neighbourhood weights:
```
1 2 3 2 1
2 4 6 4 2
3 6 9 6 3
2 4 6 4 2
1 2 3 2 1
```

I also have the following state weights,
`0, -1, 1`

Now, let's say I have cells in this configuration:
```
0 0 0 1 0
0 0 1 0 0
0 0 2 0 0
0 2 0 0 2
0 0 0 0 0
```

The neighbourhood sum would be -1 * 2 + -1 * 6 + 1 * 9 + 1 * 4 + 2 * 2 = -2 + -6 + 9 + 4 + 2 = 7
Now, looking at the following ruletable
```
0,0,0,0,1,0,0,0
1,1,2,1,1,2,1,1
2,2,2,2,2,2,1,1
```

A neighbourhood count of 7 would mean the new cell's state is 1 (the row number is the current state of the cell, the column number is the neighbourhood sum, both 0-indexed).

Before the content of the ruletable, you will need to add the following headers
```
neighbourhood:[w * (a, b), ...]
state_weights:0,1
(optional) tiling:square/hexagonal/triangular 
```

### @COLORS

This section is optional and can be used to specify the RGB colors for one or more states using lines with 4 numbers, like these:
```
0  48  48  48   dark gray
1   0 128 255   light blue
2 255 255 255   white
3 255 128   0   orange
```


# B0 rules
CAViewer makes use of a generalised B0 algorithm to emulate B0 rules.
Let's say the transition function is *c* = f(*x*, *n*) where *c* is the cell's next state, *x* is the cell's current state and *n* is the cell's neighbours.
To emulate a B0 rule, we need to ensure the background is 0 at all times. So let's say *B* is a list of background that the rules moves across.
The rule will alternate across *l* rules where *l* is the length of *B*.
Every generation, before inputting the cells into *f(x, n)*, we swap 0 and *B\[generation % l\]*. A similar function is applied to the output.

With that, we have successfully emulated a B0 rule!