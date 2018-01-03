package me.mrCookieSlime.QuestWorld.util;

public class Pair<L,R> {
	private L left;
	private R right;
	public Pair(L left, R right) {
		this.left = left;
		this.right = right;
	}
	
	public L getLeft() {
		return left;
	}
	
	public R getRight() {
		return right;
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof Pair) {
			Pair<?, ?> pair = (Pair<?, ?>)object;
			return pair.getLeft().equals(getLeft()) && pair.getRight().equals(getRight());
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int hash = 3;
		hash = 19 * hash + left.hashCode();
		hash = 19 * hash + right.hashCode();
		return hash;
	}
}
