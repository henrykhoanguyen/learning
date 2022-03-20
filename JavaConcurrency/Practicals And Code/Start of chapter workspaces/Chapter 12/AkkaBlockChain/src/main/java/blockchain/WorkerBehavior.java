package blockchain;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.javadsl.Behaviors;
import model.Block;

public class WorkerBehavior extends AbstractBehavior<WorkerBehavior.Command> {
	
	public static class Command {
		private Block block;
		private int startNonce;
		private int difficulty;
		
		public Command(Block block, int startNonce, int difficulty) {
			this.block = block;
			this.startNonce = startNonce;
			this.difficulty = difficulty;
		}
		
		public Block getBlock() {
			return block;
		}
		public int getStartNonce() {
			return startNonce;
		}
		public int getDifficulty() {
			return difficulty;
		}
		
	}

	private WorkerBehavior(ActorContext<Command> context) {
		super(context);
	}
	
	public static Behavior<Command> create() {
		return Behaviors.setup(WorkerBehavior::new);
	}

	@Override
	public Receive<Command> createReceive() {
		// TODO Auto-generated method stub
		return null;
	}

}
