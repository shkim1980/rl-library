package rlViz;

import java.io.IOException;

import rlVizLib.visualization.EnvVisualizer;

public class GraphicalDriver {

	public static void main(String [] args) throws IOException {
		

	

		RLVizFrame theFrame=new RLVizFrame();

		EnvVisualizer theEVisualizer=null;
		
		theEVisualizer=new MountainCarVisualizer();
//		TetrlaisVisualizer theEVisualizer=new TetrlaisVisualizer();
		theFrame.setEnvVisualizer((EnvVisualizer)theEVisualizer);
//		MountainCarVisualizer theAVisualizer=new MountainCarVisualizer();

//		RLGlue.RL_init();
//
//		for(int x = 0; x < 50000; ++x) {
//			RLGlue.RL_start();
//			while(RLGlue.RL_step().terminal==0){
//				try {
//					Thread.sleep(1);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//		RLGlue.RL_cleanup();


	}
}
