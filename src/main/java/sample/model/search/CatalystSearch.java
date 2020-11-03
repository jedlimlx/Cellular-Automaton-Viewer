package sample.model.search;

import sample.model.Coordinate;
import sample.model.patterns.Catalyst;
import sample.model.simulation.Grid;
import sample.model.simulation.Simulator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

class PlacedCatalyst {
    private boolean interacted = false;
    private boolean regenerated = false;
    private final int hash;
    private final Coordinate startCoordinate;
    private final List<Coordinate> coordinateList;

    public PlacedCatalyst(int hash, Coordinate startCoordinate, List<Coordinate> coordinateList) {
        this.hash = hash;
        this.startCoordinate = startCoordinate;
        this.coordinateList = coordinateList;
    }

    public int getHash() {
        return hash;
    }

    public Coordinate getStartCoordinate() {
        return startCoordinate;
    }

    public List<Coordinate> getCoordinateList() {
        return coordinateList;
    }

    public boolean hasInteracted() {
        return interacted;
    }

    public boolean hasRegenerated() {
        return regenerated;
    }

    public void setInteracted(boolean interacted) {
        this.interacted = interacted;
    }

    public void setRegenerated(boolean regenerated) {
        this.regenerated = regenerated;
    }
}

public class CatalystSearch extends SearchProgram {
    private Set<Catalyst> known;
    private final Random random = new Random();

    public CatalystSearch(CatalystSearchParameters parameters) {
        super(parameters);
    }

    @Override
    public void search(int num) {
        Simulator simulator;
        CatalystSearchParameters searchParameters = (CatalystSearchParameters) this.searchParameters;

        if (searchParameters.getBruteForce()) {
            // TODO (Brute force)
        } else {
            known = new HashSet<>();
            searchResults = new ArrayList<>(); // Initialise search results

            int initialGeneration = -1, repeatTime = -1;
            int hash, numRegen = 0, numInteracted = 0;
            List<PlacedCatalyst> placedCatalysts;
            for (int i = 0; i < num; i++) {
                simulator = new Simulator(searchParameters.getRule());
                System.out.println(i);

                initialGeneration = -1;
                placedCatalysts = randomAddCatalyst(simulator, searchParameters);
                if (placedCatalysts == null) continue;  // The catalysts overlap and are not still lives

                // Inserting the target
                simulator.insertCells(searchParameters.getTarget(), new Coordinate());

                Grid original = simulator.deepCopy();
                for (int j = 0; j < searchParameters.getMaxRepeatTime(); j++) {
                    simulator.step();

                    numRegen = 0;
                    numInteracted = 0;
                    for (PlacedCatalyst catalyst: placedCatalysts) {
                        hash = simulator.hashCode(catalyst.getCoordinateList(), catalyst.getStartCoordinate());

                        if (hash != catalyst.getHash() && !catalyst.hasInteracted()) {
                            catalyst.setInteracted(true);
                            if (initialGeneration == -1) initialGeneration = simulator.getGeneration();
                        } else if (hash == catalyst.getHash() && catalyst.hasInteracted() &&
                                !catalyst.hasRegenerated()) {
                            catalyst.setRegenerated(true);
                            repeatTime = simulator.getGeneration() - initialGeneration;
                        }

                        // To consider a catalyst valid,
                        // 1. At least one of the sub-catalysts must have been interacted with
                        // 2. All interacted catalysts must have been regenerated
                        // 3. If only one is regenerated, it's a partial
                        if (catalyst.hasInteracted()) numInteracted++;
                        if (catalyst.hasInteracted() && catalyst.hasRegenerated()) numRegen++;
                    }

                    // Every single catalyst regenerated so its not a partial
                    if (numRegen == numInteracted && numInteracted >= 1) {
                        Catalyst catalyst = new Catalyst(simulator.getRule(), original, repeatTime, false);
                        add(searchResults, catalyst);
                        break;
                    }
                }

                // Not every single catalyst regenerated so its a partial
                if (numRegen < numInteracted && numInteracted >= 1 && numRegen >= 1) {
                    Catalyst catalyst = new Catalyst(simulator.getRule(), original, repeatTime, true);
                    add(searchResults, catalyst);
                }
            }
        }
    }

    @Override
    public boolean writeToFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);

            fileWriter.write("Catalyst,RLE\n");
            for (Grid pattern: searchResults) {
                fileWriter.write(pattern + "," + pattern.toRLE() + "\n");
            }

            fileWriter.close();
            return true;
        } catch (IOException exception) {
            LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).
                    log(Level.WARNING, exception.getMessage());
            return false;
        }
    }

    private List<PlacedCatalyst> randomAddCatalyst(Simulator grid, CatalystSearchParameters searchParameters) {
        int index;
        Grid catalyst;
        Coordinate coordinate;
        ArrayList<PlacedCatalyst> placedCatalysts = new ArrayList<>();
        for (int i = 0; i < searchParameters.getNumCatalysts(); i++) {
            index = random.nextInt(searchParameters.getCoordinateList().size());
            coordinate = searchParameters.getCoordinateList().get(index);

            index = random.nextInt(searchParameters.getCatalysts().size());
            catalyst = searchParameters.getCatalysts().get(index).deepCopy();

            if (searchParameters.getRotateCatalyst()) {
                catalyst.updateBounds();
                for (int j = 0; j < random.nextInt(4); j++)
                    catalyst.rotateCW(catalyst.getBounds().getValue0(), catalyst.getBounds().getValue1());
            }

            if (searchParameters.getFlipCatalyst()) {
                catalyst.updateBounds();

                int randomInt = random.nextInt(4);
                if (randomInt == 0) {
                    catalyst.reflectCellsX(catalyst.getBounds().getValue0(), catalyst.getBounds().getValue1());
                } else if (randomInt == 1) {
                    catalyst.reflectCellsY(catalyst.getBounds().getValue0(), catalyst.getBounds().getValue1());
                } else if (randomInt == 2) {
                    catalyst.reflectCellsX(catalyst.getBounds().getValue0(), catalyst.getBounds().getValue1());
                    catalyst.reflectCellsY(catalyst.getBounds().getValue0(), catalyst.getBounds().getValue1());
                }
            }

            grid.insertCells(catalyst, coordinate);

            List<Coordinate> bfsResult = catalyst.bfs(1, searchParameters.getRule().getNeighbourhood());

            Coordinate coordinate3 = new Coordinate();
            for (Coordinate coordinate2: bfsResult) {
                if (coordinate3.getX() > coordinate2.getX())
                    coordinate3 = new Coordinate(coordinate2.getX(), coordinate3.getY());
                if (coordinate3.getY() > coordinate2.getY())
                    coordinate3 = new Coordinate(coordinate3.getX(), coordinate2.getY());
            }

            int hash = catalyst.hashCode(bfsResult, coordinate3);

            for (int j = 0; j < bfsResult.size(); j++) bfsResult.set(j, bfsResult.get(j).add(coordinate));
            for (Coordinate coordinate2: bfsResult) {
                if (coordinate.getX() > coordinate2.getX())
                    coordinate = new Coordinate(coordinate2.getX(), coordinate.getY());
                if (coordinate.getY() > coordinate2.getY())
                    coordinate = new Coordinate(coordinate.getX(), coordinate2.getY());
            }

            placedCatalysts.add(new PlacedCatalyst(hash, coordinate, bfsResult));
        }

        // if (!grid.identify(2).toString().equals("Still Life")) {
        //    return null;
        // }

        return placedCatalysts;
    }
}