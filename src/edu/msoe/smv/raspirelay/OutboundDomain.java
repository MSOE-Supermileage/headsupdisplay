/**
 * Project: AndroidRelay
 * Author: Austin Hartline
 * Date: 12/7/14
 */

package edu.msoe.smv.raspirelay;

import java.io.DataOutputStream;
import java.util.ArrayList;

/**
 * @author austin
 * @version 2014.12.07
 */
public class OutboundDomain {

    /**
     * This file splits logging data between logcat, the logger, and outbound to all connected webclients.
     * <p/>
     * Some decisions need to be made about verbosity at each end
     */

    Logger logger;

    ArrayList<DataOutputStream> webclients;

    // logcat

    // sqlite database...
}
