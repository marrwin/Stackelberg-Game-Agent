import comp34120.ex2.PlayerImpl;
import comp34120.ex2.PlayerType;
import comp34120.ex2.FollowerEstimator;
import comp34120.ex2.Record;
import java.util.Arrays;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import org.ejml.simple.SimpleMatrix;

/**
 * A very simple leader implementation that only generates random prices
 * @author Xin
 */
final class SimpleLeader
	extends PlayerImpl
{

	private FollowerEstimator followerEstimator;

	private SimpleLeader()
		throws RemoteException, NotBoundException
	{
		super(PlayerType.LEADER, "Group 19 Leader");
		followerEstimator = null;
	}

	private Record[] getHistoricData(int currentDate) throws RemoteException{
		Record[] output = new Record[currentDate - 1];
		for(int index = 0;index < output.length; index++){
			output[index] = m_platformStub.query(PlayerType.LEADER,index  + 1);
		}
		return output;
	}

	@Override
	public void goodbye()
		throws RemoteException
	{
		ExitTask.exit(500);
	}

	/**
	 * To inform this instance to proceed to a new simulation day
	 * @param p_date The date of the new day
	 * @throws RemoteException
	 */
	@Override
	public void proceedNewDay(int p_date)
		throws RemoteException
	{
		if (followerEstimator == null) {
			Record[] historicData = getHistoricData(p_date);
			followerEstimator = new FollowerEstimator(0.99, historicData);
		}

		double[] parameter = followerEstimator.getFollowerParameters();
		m_platformStub.publishPrice(m_type, (float)genLeaderPrice(parameter[0],parameter[1]));

		Record newData = m_platformStub.query(PlayerType.LEADER, p_date);
		followerEstimator.update(newData.m_leaderPrice, newData.m_followerPrice);
	}

	/**
	* Generate the best strategy for the leader (UL) from the reaction function
	* calculated for the follower in the form of: R(UL) = mUL + C
	* m: the gradient of the reaction function
	* c: unit cost
	* the first derivative of profit function JL(UL, UF) which is JL(UL, R(UL))
	* Profit function: (uL - cL )SL(uL ,uF )
	*	Therefore,
	* JL(UL, R(UL)) = (uL - 1)(2 - uL + 0.3 R(uL))
	* = (1)(2-uL+0.3(muL+C)) + (uL-1)(-1+0.3m)
	*	= 2-uL+0.3muL+0.3c-uL+0.3muL+1-0.3m
	*	= 3-2uL+0.6muL+0.3c-0.3m
	*	Second order derivate is < 0, therefore it is a local maxima
	*	Solving for leader's best strategy uL by setting first derivate = 0
	*	3-2uL+0.6muL+0.3c-0.3m = 0
	* -2uL+0.6muL = -3+0.3m-0.3c
	*	uL(-2+0.6m)=-3+0.3m-0.3c
	* uL = (3+0.3m-0.3c) / (-2+0.6m)
	*/
	private double genLeaderPrice(double m, double c)
	{
		return ((-3 + 0.3*(m) - 0.3*(c)) / (-2 + 0.6*(m)));
	}

	public static void main(final String[] p_args)
		throws RemoteException, NotBoundException
	{
		new SimpleLeader();
	}

	/**
	 * The task used to automatically exit the leader process
	 * @author Xin
	 */
	private static class ExitTask
		extends TimerTask
	{
		static void exit(final long p_delay)
		{
			(new Timer()).schedule(new ExitTask(), p_delay);
		}

		@Override
		public void run()
		{
			System.exit(0);
		}
	}
}
