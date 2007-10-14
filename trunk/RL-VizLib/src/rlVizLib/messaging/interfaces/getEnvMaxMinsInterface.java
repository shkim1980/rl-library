package rlVizLib.messaging.interfaces;

public interface getEnvMaxMinsInterface {
	public int getNumVars();
	public double getMinValueForQuerableVariable(int dimension);
	public double getMaxValueForQuerableVariable(int dimension);

}