package comp34120.ex2;
import org.ejml.simple.SimpleMatrix;
import comp34120.ex2.Record;

public class FollowerEstimator{

	//follower reaction function parameters
	SimpleMatrix parameters;

	//Pt
	SimpleMatrix pt;

	//forgeting factor
	double lambda = 0.99;

	//constructor
	public FollowerEstimator(double lambda, Record[] historicData){
		//inilize parameters
		this.lambda = lambda;
		parameters = new SimpleMatrix(
		new double[][] {
			new double[] {0d},
			new double[] {0d}
		}
		);

		pt = new SimpleMatrix(
		new double[][] {
			new double[] {0d, 0d},
			new double[] {0d, 0d}
		}
		);
		init(historicData);
	}

	private void init(Record[] historicData){
		//set pt
		for(int index = 0; index < historicData.length; index++){
			//current forgeting factor
			double currentForgetingFactor = Math.pow(lambda,historicData.length-(index+1));
			//phiXt
			SimpleMatrix phiXt = new SimpleMatrix(
				new double[][] {
					new double[] {1d},
					new double[] {historicData[index].m_leaderPrice}
				}
			);
			SimpleMatrix resultpt = phiXt.mult(phiXt.transpose()).scale(currentForgetingFactor);
			pt = pt.plus(resultpt);

			//calcualte parameters
			parameters = parameters.plus(phiXt.scale(historicData[index].m_followerPrice).scale(currentForgetingFactor));
		}

		//parameters
		parameters = pt.invert().mult(parameters);

	}

	public void update(double newLeader, double newFollower){
		// Compute Lt+1
		SimpleMatrix lt1;
		SimpleMatrix phiXt1 = new SimpleMatrix(
			new double[][] {
				new double[] {1d},
				new double[] {newLeader}
			}
		);
		SimpleMatrix numerator = pt.mult(phiXt1);
		double denominator = lambda + phiXt1.transpose().mult(pt).mult(phiXt1).get(0,0);
		lt1 = numerator.scale(1/denominator);

		// Update Pt
		numerator = numerator.mult(phiXt1.transpose()).mult(pt);
		pt = pt.minus(numerator.scale(1/denominator)).scale(1/lambda);

		// update parameter
		double preditctionError = newFollower - phiXt1.transpose().mult(parameters).get(0,0);
		parameters = parameters.plus(lt1.scale(preditctionError));
	}

	public double[] getFollowerParameters(){
		//the returned is [m,c] in the this order
		return new double[]{parameters.get(1,0),parameters.get(0,0)};
	}
}
