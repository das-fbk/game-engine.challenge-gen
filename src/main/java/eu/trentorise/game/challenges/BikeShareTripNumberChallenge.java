package eu.trentorise.game.challenges;

import eu.trentorise.game.challenges.exception.UndefinedChallengeException;
import eu.trentorise.game.challenges.model.ChallengeType;

public class BikeShareTripNumberChallenge extends TripNumberChallenge {

    public BikeShareTripNumberChallenge(String templateDir) {
        super(templateDir);
        this.templateName = "BikeShareTravelTemplate.drt";
        this.type = ChallengeType.BSTRIPNUMBER;
    }

    @Override
    public void compileChallenge(String playerId)
            throws UndefinedChallengeException {
        if (mode == null || !mode.equals("bikesharing"))
            throw new UndefinedChallengeException("undefined challenge!");
        super.compileChallenge(playerId);
    }
}
