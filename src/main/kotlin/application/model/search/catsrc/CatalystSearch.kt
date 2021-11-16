package application.model.search.catsrc;

import application.model.Coordinate;
import application.model.patterns.Catalyst;
import application.model.search.SearchProgram;
import application.model.simulation.Grid;
import application.model.simulation.Simulator;

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
    private final Coordinate startCoordinate, startCoordinate2;
    private final List<Coordinate> coordinateList;
    private final Grid catalyst;

    public PlacedCatalyst(Grid catalyst, int hash, Coordinate startCoordinate, Coordinate startCoordinate2,
                          List<Coordinate> coordinateList) {
        this.catalyst = catalyst;
        this.hash = hash;
        this.startCoordinate = startCoordinate;
        this.startCoordinate2 = startCoordinate2;
        this.coordinateList = coordinateList;
    }

    public Grid getCatalyst() {
        return catalyst;
    }

    public int getHash() {
        return hash;
    }

    public Coordinate getStartCoordinate() {
        return startCoordinate;
    }

    public Coordinate getStartCoordinate2() {
        return startCoordinate2;
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

            long startTime = System.currentTimeMillis();
            int initialGeneration, repeatTime = -1;
            int hash, numRegen, numInteracted;
            List<PlacedCatalyst> usedCatalysts, placedCatalysts;
            for (int i = 0; i < num; i++) {
                // Check if the search should stop
                if (stop) break;

                simulator = new Simulator(searchParameters.getRule());

                initialGeneration = -1;
                usedCatalysts = new ArrayList<>();
                placedCatalysts = randomAddCatalyst(simulator, searchParameters);
                if (placedCatalysts == null) continue;  // The catalysts overlap and are not still lives

                // Inserting the target
                simulator.insertCells(searchParameters.getTarget(), new Coordinate());

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
                            usedCatalysts.add(catalyst);
                            catalyst.setRegenerated(true);
                            repeatTime = simulator.getGeneration() - initialGeneration;
                        }

                        // To consider a catalyst valid,
                        // 1. At least one of the sub-catalysts must have been interacted with
                        // 2. All interacted catalysts must have been regenerated
                        if (catalyst.hasInteracted()) numInteracted++;
                        if (catalyst.hasInteracted() && catalyst.hasRegenerated()) numRegen++;
                    }

                    // Every single catalyst regenerated
                    if (numRegen == numInteracted && numInteracted >= 1) {
                        Grid original = new Grid();
                        original.insertCells(searchParameters.getTarget(), new Coordinate());
                        for (PlacedCatalyst catalyst: usedCatalysts)
                            original.insertCells(catalyst.getCatalyst(), catalyst.getStartCoordinate2());

                        Catalyst catalyst = new Catalyst(simulator.getRule(), original, repeatTime);

                        if (!known.contains(catalyst)) {
                            add(searchResults, catalyst);
                            add(known, catalyst);
                        }

                        break;
                    }
                }

                synchronized (this) {  // To avoid race conditions
                    if (numSearched % 5000 == 0 && numSearched != 0) {
                        System.out.println(numSearched + " potential catalysts searched (" +
                                5000000 / (System.currentTimeMillis() - startTime) +
                                " potential catalysts/s), " + searchResults.size() + " catalysts found!");
                        startTime = System.currentTimeMillis();
                    }

                    numSearched++;
                }
            }
        }
    }

    @Override
    public boolean writeToFile(File file) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write("# Running search in " + ((CatalystSearchParameters) searchParameters).getRule() +
                    "\n");
            fileWriter.write("Catalyst,RLE\n");
            for (int i = 0; i < searchResults.size(); i++) {
                fileWriter.write(searchResults.get(i) + "," + searchResults.get(i).toRLE() + "\n");
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
            Coordinate originalCoordinate = coordinate;

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

            placedCatalysts.add(new PlacedCatalyst(catalyst, hash, coordinate, originalCoordinate, bfsResult));
        }

        // Ensuring the catalysts are stable
        int hash = grid.hashCode();
        grid.step();

        if (hash != grid.hashCode()) {
            return null;
        }

        return placedCatalysts;
    }
}