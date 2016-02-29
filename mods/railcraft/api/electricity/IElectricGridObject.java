/*
 * ******************************************************************************
 *  Copyright 2011-2015 CovertJaguar
 *
 *  This work (the API) is licensed under the "MIT" License, see LICENSE.md for details.
 * ***************************************************************************
 */

package mods.railcraft.api.electricity;

import mods.railcraft.api.core.WorldCoordinate;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import java.util.*;

/**
 * Any Electric Track needs to implement this interface on either the track
 * TileEntity or ITrackInstance object.
 * <p/>
 * Other blocks can also implement this on their tile entity to gain access to
 * the electricGrid.
 *
 * @author CovertJaguar <http://www.railcraft.info/>
 */
public interface IElectricGridObject {

    double MAX_CHARGE = 10000.0;
    double TRACK_LOSS_PER_TICK = 0.05;
    int SEARCH_INTERVAL = 64;
    Random rand = new Random();

    ChargeHandler getChargeHandler();

    TileEntity getTile();

    final class ChargeHandler {

        public enum ConnectType {

            TRACK {
                @Override
                public Map<WorldCoordinate, EnumSet<ConnectType>> getPossibleConnectionLocations(IElectricGridObject gridObject) {
                    int dim = gridObject.getTile().getWorld().provider.getDimensionId();
                    int x = gridObject.getTile().xCoord;
                    int y = gridObject.getTile().yCoord;
                    int z = gridObject.getTile().zCoord;
                    Map<WorldCoordinate, EnumSet<ConnectType>> positions = new HashMap<WorldCoordinate, EnumSet<ConnectType>>();

                    EnumSet<ConnectType> all = EnumSet.allOf(ConnectType.class);
                    EnumSet<ConnectType> notWire = EnumSet.complementOf(EnumSet.of(ConnectType.WIRE));
                    EnumSet<ConnectType> track = EnumSet.of(ConnectType.TRACK);

                    positions.put(new WorldCoordinate(dim, x + 1, y, z), notWire);
                    positions.put(new WorldCoordinate(dim, x - 1, y, z), notWire);

                    positions.put(new WorldCoordinate(dim, x + 1, y + 1, z), track);
                    positions.put(new WorldCoordinate(dim, x + 1, y - 1, z), track);

                    positions.put(new WorldCoordinate(dim, x - 1, y + 1, z), track);
                    positions.put(new WorldCoordinate(dim, x - 1, y - 1, z), track);

                    positions.put(new WorldCoordinate(dim, x, y - 1, z), all);

                    positions.put(new WorldCoordinate(dim, x, y, z + 1), notWire);
                    positions.put(new WorldCoordinate(dim, x, y, z - 1), notWire);

                    positions.put(new WorldCoordinate(dim, x, y + 1, z + 1), track);
                    positions.put(new WorldCoordinate(dim, x, y - 1, z + 1), track);

                    positions.put(new WorldCoordinate(dim, x, y + 1, z - 1), track);
                    positions.put(new WorldCoordinate(dim, x, y - 1, z - 1), track);
                    return positions;
                }

            },
            WIRE {
                @Override
                public Map<WorldCoordinate, EnumSet<ConnectType>> getPossibleConnectionLocations(IElectricGridObject gridObject) {
                    int dim = gridObject.getTile().getWorldObj().provider.dimensionId;
                    int x = gridObject.getTile().xCoord;
                    int y = gridObject.getTile().yCoord;
                    int z = gridObject.getTile().zCoord;
                    Map<WorldCoordinate, EnumSet<ConnectType>> positions = new HashMap<WorldCoordinate, EnumSet<ConnectType>>();

                    EnumSet<ConnectType> all = EnumSet.allOf(ConnectType.class);
                    EnumSet<ConnectType> notTrack = EnumSet.complementOf(EnumSet.of(ConnectType.TRACK));

                    positions.put(new WorldCoordinate(dim, x + 1, y, z), notTrack);
                    positions.put(new WorldCoordinate(dim, x - 1, y, z), notTrack);
                    positions.put(new WorldCoordinate(dim, x, y + 1, z), all);
                    positions.put(new WorldCoordinate(dim, x, y - 1, z), notTrack);
                    positions.put(new WorldCoordinate(dim, x, y, z + 1), notTrack);
                    positions.put(new WorldCoordinate(dim, x, y, z - 1), notTrack);
                    return positions;
                }

            },
            BLOCK {
                @Override
                public Map<WorldCoordinate, EnumSet<ConnectType>> getPossibleConnectionLocations(IElectricGridObject gridObject) {
                    int dim = gridObject.getTile().getWorldObj().provider.dimensionId;
                    int x = gridObject.getTile().xCoord;
                    int y = gridObject.getTile().yCoord;
                    int z = gridObject.getTile().zCoord;
                    Map<WorldCoordinate, EnumSet<ConnectType>> positions = new HashMap<WorldCoordinate, EnumSet<ConnectType>>();

                    EnumSet<ConnectType> all = EnumSet.allOf(ConnectType.class);

                    positions.put(new WorldCoordinate(dim, x + 1, y, z), all);
                    positions.put(new WorldCoordinate(dim, x - 1, y, z), all);
                    positions.put(new WorldCoordinate(dim, x, y + 1, z), all);
                    positions.put(new WorldCoordinate(dim, x, y - 1, z), all);
                    positions.put(new WorldCoordinate(dim, x, y, z + 1), all);
                    positions.put(new WorldCoordinate(dim, x, y, z - 1), all);
                    return positions;
                }

            };

            public abstract Map<WorldCoordinate, EnumSet<ConnectType>> getPossibleConnectionLocations(IElectricGridObject gridObject);

        }

        private final IElectricGridObject gridObject;
        private ElectricGrid electricGrid;
        private final ConnectType type;
        private double draw, lastTickDraw;
        private final double lossPerTick;
        private int clock = rand.nextInt();

        public ChargeHandler(IElectricGridObject gridObject, ConnectType type) {
            this(gridObject, type, type == ConnectType.TRACK ? TRACK_LOSS_PER_TICK : 0.0);
        }

        public ChargeHandler(IElectricGridObject gridObject, ConnectType type, double lossPerTick) {
            this.gridObject = gridObject;
            this.type = type;
            this.lossPerTick = lossPerTick;
        }

        public ElectricGrid getElectricGrid() {
            if (electricGrid == null)
                electricGrid = new ElectricGrid();
            else if (electricGrid.getReplacement() != null)
                electricGrid = electricGrid.getReplacement();
            return electricGrid;
        }

        public Map<WorldCoordinate, EnumSet<ConnectType>> getPossibleConnectionLocations() {
            return type.getPossibleConnectionLocations(gridObject);
        }

        public double getLosses() {
            return lossPerTick;
        }

        public double getDraw() {
            return draw;
        }

        public ConnectType getType() {
            return type;
        }

        public void setCharge(double charge) {
            this.charge = charge;
        }

        public void addCharge(double charge) {
            this.charge += charge;
        }

        /**
         * Remove up to the requested amount of charge and returns the amount
         * removed.
         *
         * @return charge removed
         */
        public double removeCharge(double request) {
            if (charge >= request) {
                charge -= request;
                lastTickDraw += request;
                return request;
            }
            double ret = charge;
            charge = 0.0;
            lastTickDraw += ret;
            return ret;
        }

        private void removeLosses() {
            if (lossPerTick > 0.0)
                if (charge >= lossPerTick)
                    charge -= lossPerTick;
                else
                    charge = 0.0;
        }

        /**
         * Must be called once per tick by the owning object. Server side only.
         */
        public void tick() {
            clock++;
            removeLosses();

            draw = (draw * 49.0 + lastTickDraw) / 50.0;
            lastTickDraw = 0.0;

            if (charge <= 0.0)
                return;

            if (clock % SEARCH_INTERVAL == 0) {
                neighbors.clear();
                Set<IElectricGridObject> connections = GridTools.getMutuallyConnectedObjects(gridObject);
                for (IElectricGridObject t : connections) {
                    neighbors.add(t.getChargeHandler());
                }
            }

            Iterator<ChargeHandler> it = neighbors.iterator();
            while (it.hasNext()) {
                ChargeHandler ch = it.next();
                if (ch.gridObject.getTile().isInvalid())
                    it.remove();
            }
            for (ChargeHandler t : neighbors) {
                balance(t);
            }
        }

        /**
         * Must be called by the owning object's save function.
         */
        public void writeToNBT(NBTTagCompound nbt) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setDouble("charge", charge);
            nbt.setTag("chargeHandler", tag);
        }

        /**
         * Must be called by the owning object's load function.
         */
        public void readFromNBT(NBTTagCompound nbt) {
            NBTTagCompound tag = nbt.getCompoundTag("chargeHandler");
            charge = tag.getDouble("charge");
        }

    }

}
