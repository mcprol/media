import { MediaPluginProtocol, MediaFetchOptions, MediaResponse, MediaAlbumResponse, MediaSaveOptions, MediaAlbumCreate } from './definitions';
export declare class Media implements MediaPluginProtocol {
    getMedias(options?: MediaFetchOptions): Promise<MediaResponse>;
    getAlbums(): Promise<MediaAlbumResponse>;
    savePhoto(options?: MediaSaveOptions): Promise<void>;
    saveVideo(options?: MediaSaveOptions): Promise<void>;
    saveGif(options?: MediaSaveOptions): Promise<void>;
    createAlbum(options: MediaAlbumCreate): Promise<void>;
}
