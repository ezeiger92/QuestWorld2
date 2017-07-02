package me.mrCookieSlime.QuestWorld.utils;

import java.util.List;

import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;

public class DummyEconomy extends AbstractEconomy {

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String arg0) {
		return true;
	}

	@Override
	public boolean createPlayerAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public String currencyNamePlural() {
		return "dummies";
	}

	@Override
	public String currencyNameSingular() {
		return "dummy";
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, double arg1) {
		return new EconomyResponse(arg1, 0.0, ResponseType.SUCCESS, null);
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
		return new EconomyResponse(arg2, 0.0, ResponseType.SUCCESS, null);
	}

	@Override
	public String format(double arg0) {
		return String.valueOf(arg0);
	}

	@Override
	public int fractionalDigits() {
		return 0;
	}

	@Override
	public double getBalance(String arg0) {
		return 0;
	}

	@Override
	public double getBalance(String arg0, String arg1) {
		return 0;
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public String getName() {
		return "DummyEconomy";
	}

	@Override
	public boolean has(String arg0, double arg1) {
		return false;
	}

	@Override
	public boolean has(String arg0, String arg1, double arg2) {
		return false;
	}

	@Override
	public boolean hasAccount(String arg0) {
		return true;
	}

	@Override
	public boolean hasAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, double arg1) {
		return new EconomyResponse(arg1, 0.0, ResponseType.SUCCESS, null);
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		return new EconomyResponse(arg2, 0.0, ResponseType.SUCCESS, null);
	}

}
