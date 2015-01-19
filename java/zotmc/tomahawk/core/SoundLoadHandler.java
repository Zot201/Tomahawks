package zotmc.tomahawk.core;

import static com.google.common.base.Preconditions.checkState;
import static cpw.mods.fml.relauncher.Side.CLIENT;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;
import zotmc.tomahawk.data.ModData.AxeTomahawk;
import zotmc.tomahawk.data.ReflData.ClientRefls;

import com.google.common.base.Throwables;

import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(CLIENT)
public class SoundLoadHandler {
	
	SoundLoadHandler() {
		checkState(ClientRefls.soundManagers.isPresent());
	}
	
	private void addSound(SoundManager manager, String path) {
		try {
			ClientRefls.soundManagers.get().addSound.invoke(manager, path);
		} catch (Throwable e) {
			throw Throwables.propagate(e);
		}
	}
	
	@ForgeSubscribe public void onSoundLoad(SoundLoadEvent event) {
		@SuppressWarnings("deprecation") SoundManager m = event.manager;
		
		addSound(m, AxeTomahawk.DOMAIN + ":random/tomahawk.ogg");
		addSound(m, AxeTomahawk.DOMAIN + ":random/tomahawk_hit1.ogg");
		addSound(m, AxeTomahawk.DOMAIN + ":random/tomahawk_hit2.ogg");
		addSound(m, AxeTomahawk.DOMAIN + ":random/tomahawk_hit3.ogg");
		addSound(m, AxeTomahawk.DOMAIN + ":random/tomahawk_hit4.ogg");
	}

}
