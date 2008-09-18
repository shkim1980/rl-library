package org.rlcommunity.environments.cartpole;

import org.rlcommunity.environments.cartpole.messages.*;
import rlVizLib.Environments.EnvironmentBase;
import rlVizLib.general.ParameterHolder;
import rlVizLib.general.hasVersionDetails;
import rlVizLib.messaging.NotAnRLVizMessageException;
import rlVizLib.messaging.environment.EnvironmentMessageParser;
import rlVizLib.messaging.environment.EnvironmentMessages;
import rlVizLib.messaging.interfaces.HasAVisualizerInterface;
import rlglue.types.Action;
import rlglue.types.Observation;
import rlglue.types.Random_seed_key;
import rlglue.types.Reward_observation;
import rlglue.types.State_key;

/**
 * This is based on David Finton's code from:
 * http://pages.cs.wisc.edu/~finton/poledriver.html which in turn is credited to
 * The Barto, Sutton, and Anderson cart-pole simulation. 
 * Available (not in 2008) by anonymous ftp from ftp.gte.com, as 
 * /pub/reinforcement-learning/pole.c.
 * 
 * @author btanner
 */
public class CartPole extends EnvironmentBase implements HasAVisualizerInterface {

    final static double GRAVITY = 9.8;
    final static double MASSCART = 1.0;
    final static double MASSPOLE = 0.1;
    final static double TOTAL_MASS = (MASSPOLE + MASSCART);
    final static double LENGTH = 0.5;	  /* actually half the pole's length */

    final static double POLEMASS_LENGTH = (MASSPOLE * LENGTH);
    final static double FORCE_MAG = 10.0;
    final static double TAU = 0.02;	  /* seconds between state updates */

    final static double FOURTHIRDS = 4.0d/3.0d;
    final static double DEFAULTLEFTCARTBOUND = -2.4;
    final static double DEFAULTRIGHTCARTBOUND = 2.4;
    final static double DEFAULTLEFTANGLEBOUND = -Math.toRadians(12.0d);
    final static double DEFAULTRIGHTANGLEBOUND = Math.toRadians(12.0d);
    double leftCartBound;
    double rightCartBound;
    double leftAngleBound;
    double rightAngleBound;

    //State variables
    double x;			/* cart position, meters */
    double x_dot;			/* cart velocity */
    double theta;			/* pole angle, radians */
    double theta_dot;		/* pole angular velocity */

    
    public CartPole() {
        this(getDefaultParameters());
    }

    public CartPole(ParameterHolder p) {
        super();
        if (p != null) {
            if (!p.isNull()) {
                leftAngleBound = p.getDoubleParam("leftAngle");
                rightAngleBound = p.getDoubleParam("rightAngle");
                this.leftCartBound = p.getDoubleParam("leftCart");
                rightCartBound = p.getDoubleParam("rightCart");

            }
        }
    }

    public static ParameterHolder getDefaultParameters() {
        ParameterHolder p = new ParameterHolder();
        rlVizLib.utilities.UtilityShop.setVersionDetails(p, new DetailsProvider());

        p.addDoubleParam("Left Terminal Angle", DEFAULTLEFTANGLEBOUND);
        p.addDoubleParam("Right Terminal Angle", DEFAULTRIGHTANGLEBOUND);
        p.addDoubleParam("Terminal Left Cart Position", DEFAULTLEFTCARTBOUND);
        p.addDoubleParam("Terminal Right Cart Position", DEFAULTRIGHTCARTBOUND);

        p.setAlias("leftCart", "Terminal Left Cart Position");
        p.setAlias("rightCart", "Terminal Right Cart Position");
        p.setAlias("leftAngle", "Left Terminal Angle");
        p.setAlias("rightAngle", "Right Terminal Angle");
        return p;
    }


    /*RL GLUE METHODS*/
    public String env_init() {
        double xMin = leftCartBound;
        double xMax = rightCartBound;

        //Dots are guesses
        double xDotMin = -4.0d;
        double xDotMax = 4.0d;
        double thetaMin = leftAngleBound;
        double thetaMax = rightAngleBound;
        double thetaDotMin = -6.0d;
        double thetaDotMax = 6.0d;

        String taskSpec = "2:e:4_[f,f,f,f]_";
        taskSpec += "[" + xMin + "," + xMax + "]_";
        taskSpec += "[" + xDotMin + "," + xDotMax + "]_";
        taskSpec += "[" + thetaMin + "," + thetaMax + "]_";
        taskSpec += "[" + thetaDotMin + "," + thetaDotMax + "]";
        taskSpec += ":1_[i]_[0,1]:[-1,0]";
        x = 0.0f;
        x_dot = 0.0f;
        theta = 0.0f;
        theta_dot = 0.0f;

        return taskSpec;
    }

    public Observation env_start() {
        x = 0.0f;
        x_dot = 0.0f;
        theta = 0.0f;
        theta_dot = 0.0f;
        return makeObservation();
    }

    public Reward_observation env_step(Action action) {
        double xacc;
        double thetaacc;
        double force;
        double costheta;
        double sintheta;
        double temp;

        if (action.intArray[0] > 0) {
            force = FORCE_MAG;
        } else {
            force = -FORCE_MAG;
        }

        costheta = Math.cos(theta);
        sintheta = Math.sin(theta);

        temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta) / TOTAL_MASS;

        thetaacc = (GRAVITY * sintheta - costheta * temp) / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta / TOTAL_MASS));

        xacc = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;

        /*** Update the four state variables, using Euler's method. ***/
        x += TAU * x_dot;
        x_dot += TAU * xacc;
        theta += TAU * theta_dot;
        theta_dot += TAU * thetaacc;

        while (theta > Math.PI) {
            theta -= 2.0d * Math.PI;
        }
        while (theta < -Math.PI) {
            theta += 2.0d * Math.PI;
        }



        if (inFailure())
        {
            return new Reward_observation(-1.0d, makeObservation(), 1);
        } else {
            return new Reward_observation(1.0d, makeObservation(), 0);
        }
    }

    public void env_cleanup() {
    }

    public Random_seed_key env_get_random_seed() {
        return null;
    }

    public State_key env_get_state() {
        return null;
    }

    public String env_message(String theMessage) {
        EnvironmentMessages theMessageObject;
        try {
            theMessageObject = EnvironmentMessageParser.parseMessage(theMessage);
        } catch (NotAnRLVizMessageException e) {
            System.err.println("Someone sent Cartpole a message that wasn't RL-Viz compatible");
            return "I only respond to RL-Viz messages!";
        }

        if (theMessageObject.canHandleAutomatically(this)) {
            return theMessageObject.handleAutomatically(this);
        }

//		If it wasn't handled automatically, maybe its a custom Mountain Car Message
        if (theMessageObject.getTheMessageType() == rlVizLib.messaging.environment.EnvMessageType.kEnvCustom.id()) {

            String theCustomType = theMessageObject.getPayLoad();


            if (theCustomType.equals("GETCARTPOLETRACK")) {
                //It is a request for the state
                CartpoleTrackResponse theResponseObject = new CartpoleTrackResponse(leftCartBound, rightCartBound, leftAngleBound, rightAngleBound);
                return theResponseObject.makeStringResponse();
            }
        }
        System.err.println("We need some code written in Env Message for Cartpole.. unknown request received: " + theMessage);
        Thread.dumpStack();
        return null;
    }

    public void env_set_random_seed(Random_seed_key key) {
    }

    public void env_set_state(State_key key) {
    }

    /*END OF RL_GLUE FUNCTIONS*/

    /*RL-VIZ Requirements*/
    @Override
    protected Observation makeObservation() {
        Observation returnObs = new Observation(0, 4);
        returnObs.doubleArray[0] = x;
        returnObs.doubleArray[1] = x_dot;
        returnObs.doubleArray[2] = theta;
        returnObs.doubleArray[3] = theta_dot;

        return returnObs;
    }

    /*END OF RL-VIZ REQUIREMENTS*/
    /*CART POLE SPECIFIC FUNCTIONS*/
    private boolean inFailure() {
        if (x < leftCartBound || x > rightCartBound || theta < leftAngleBound || theta > rightAngleBound) {
            return true;
        } /* to signal failure */
        return false;
    }

    public double getLeftCartBound() {
        return this.leftCartBound;
    }

    public double getRightCartBound() {
        return this.rightCartBound;
    }

    public double getRightAngleBound() {
        return this.rightAngleBound;
    }

    public double getLeftAngleBound() {
        return this.leftAngleBound;
    }

    
    public String getVisualizerClassName() {
        return "org.rlcommunity.environments.cartpole.visualizer.CartPoleVisualizer";
    }
}


/**
 * This is a little helper class that fills in the details about this environment
 * for the fancy print outs in the visualizer application.
 * @author btanner
 */
class DetailsProvider implements hasVersionDetails {

    public String getName() {
        return "Cart-Pole .9 Beta";
    }

    public String getShortName() {
        return "Cart-Pole";
    }

    public String getAuthors() {
        return "Brian Tanner from David Finton from Sutton and Anderson";
    }

    public String getInfoUrl() {
        return "http://code.google.com/p/rl-library/wiki/CartPole";
    }

    public String getDescription() {
        return "RL-Library Java Version of the classic Cart-Pole RL-Problem.";
    }
}


