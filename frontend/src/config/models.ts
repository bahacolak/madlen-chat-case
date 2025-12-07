export const IMAGE_GENERATION_MODELS = [
    'google/gemma-3-4b-it:free',
];

export const isImageGenerationModel = (modelId: string | null): boolean => {
    if (!modelId) return false;
    return IMAGE_GENERATION_MODELS.some(id => modelId.includes(id.split(':')[0]));
};
