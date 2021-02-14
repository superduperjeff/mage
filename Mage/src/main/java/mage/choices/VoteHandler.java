package mage.choices;

import mage.abilities.Ability;
import mage.game.Game;
import mage.game.events.GameEvent;
import mage.players.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author TheElk801
 */
public abstract class VoteHandler<T> {

    protected final Map<UUID, List<T>> playerMap = new HashMap<>();

    public void doVotes(Ability source, Game game) {
        playerMap.clear();
        for (UUID playerId : game.getState().getPlayersInRange(source.getControllerId(), game)) {
            Player player = game.getPlayer(playerId);
            if (player == null) {
                continue;
            }
            GameEvent event = GameEvent.getEvent(GameEvent.EventType.VOTE, playerId, source, playerId, 1);
            int voteCount = event.getAmount();
            for (int i = 0; i < voteCount; i++) {
                T vote = playerChoose(player, player, source, game);
                if (vote == null) {
                    continue;
                }
                playerMap.computeIfAbsent(playerId, x -> new ArrayList<>()).add(vote);
            }
        }
    }

    public abstract T playerChoose(Player player, Player decidingPlayer, Ability source, Game game);

    public List<T> getVotes(UUID playerId) {
        return playerMap.computeIfAbsent(playerId, x -> new ArrayList<>());
    }

    public int getVoteCount(T vote) {
        return playerMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(vote::equals)
                .mapToInt(x -> x ? 1 : 0)
                .sum();
    }

    public List<UUID> getVotedFor(T vote) {
        return playerMap
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue() != null && entry.getValue().contains(vote))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public Set<T> getMostVoted() {
        Map<T, Integer> map = new HashMap<>();
        playerMap
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(t -> map.compute(t, (s, i) -> i == null ? 1 : Integer.sum(i, 1)));
        int max = map.values().stream().mapToInt(x -> x).max().orElse(0);
        return map
                .entrySet()
                .stream()
                .filter(e -> e.getValue() >= max)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
