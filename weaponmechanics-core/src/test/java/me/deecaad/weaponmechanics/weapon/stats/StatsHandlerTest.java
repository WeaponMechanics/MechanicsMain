package me.deecaad.weaponmechanics.weapon.stats;

import me.deecaad.weaponmechanics.wrappers.PlayerWrapper;
import me.deecaad.weaponmechanics.wrappers.StatsData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StatsHandlerTest {

    @Test
    void saveDoesNotRequestDatabaseWhenStatsAreUnavailable() {
        PlayerWrapper playerWrapper = mock(PlayerWrapper.class);
        StatsHandler statsHandler = new StatsHandler(null, () -> {
            throw new AssertionError("Database should not be requested when stats are unavailable");
        });

        assertDoesNotThrow(() -> statsHandler.save(playerWrapper, false));
    }

    @Test
    void saveSkipsWhenDatabaseIsUnavailable() {
        PlayerWrapper playerWrapper = mock(PlayerWrapper.class);
        when(playerWrapper.getStatsData()).thenReturn(mock(StatsData.class));
        StatsHandler statsHandler = new StatsHandler(null, () -> null);

        assertDoesNotThrow(() -> statsHandler.save(playerWrapper, false));
    }

    @Test
    void loadSkipsWhenDatabaseIsUnavailable() {
        PlayerWrapper playerWrapper = mock(PlayerWrapper.class);
        when(playerWrapper.getStatsDataUnsafe()).thenReturn(mock(StatsData.class));
        StatsHandler statsHandler = new StatsHandler(null, () -> null);

        assertDoesNotThrow(() -> statsHandler.load(playerWrapper));
        verify(playerWrapper, never()).getPlayer();
    }
}
