import { Plugins } from '@capacitor/core';
const { MediaPlugin } = Plugins;
export class Media {
    getMedias(options) {
        return MediaPlugin.getMedias(options);
    }
    getAlbums() {
        return MediaPlugin.getAlbums();
    }
    savePhoto(options) {
        return MediaPlugin.savePhoto(options);
    }
    saveVideo(options) {
        return MediaPlugin.saveVideo(options);
    }
    saveGif(options) {
        return MediaPlugin.saveGif(options);
    }
    createAlbum(options) {
        return MediaPlugin.createAlbum(options);
    }
}
//# sourceMappingURL=plugin.js.map