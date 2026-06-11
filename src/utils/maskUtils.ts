import { v4 as uuidv4 } from 'uuid';
import { Mask, SubMask, SubMaskMode, formatMaskTypeName } from '../components/panel/right/Masks';
import type { ImageDimensions } from '../hooks/useImageRenderSize';

export const REFRESHABLE_AI_MASK_TYPES = [Mask.AiForeground, Mask.AiSky, Mask.AiDepth] as const;

export const AI_MASK_TYPES = [Mask.AiSubject, ...REFRESHABLE_AI_MASK_TYPES] as const;

export const isRefreshableAiMaskType = (type?: string): type is (typeof REFRESHABLE_AI_MASK_TYPES)[number] =>
  REFRESHABLE_AI_MASK_TYPES.some((maskType) => maskType === type);

export const isAiMaskType = (type?: string): type is (typeof AI_MASK_TYPES)[number] =>
  AI_MASK_TYPES.some((maskType) => maskType === type);

export const withMaskUpdatedFlag = <T extends Record<string, unknown>>(
  parameters: T = {} as T,
  maskUpdated: boolean = true,
) => ({
  ...parameters,
  maskUpdated,
});

export const createSubMask = (
  type: Mask,
  imageDimensions: ImageDimensions,
  mode: SubMaskMode = SubMaskMode.Additive,
): SubMask => {
  const { width, height } = imageDimensions || { width: 1000, height: 1000 };
  const common = {
    id: uuidv4(),
    visible: true,
    invert: false,
    opacity: 100,
    mode,
    name: formatMaskTypeName(type),
    type,
  };

  switch (type) {
    case Mask.Radial:
      return {
        ...common,
        parameters: {
          centerX: width / 2,
          centerY: height / 2,
          radiusX: width / 4,
          radiusY: width / 4,
          rotation: 0,
          feather: 0.5,
        },
      };
    case Mask.Linear:
      return {
        ...common,
        parameters: { startX: width * 0.25, startY: height / 2, endX: width * 0.75, endY: height / 2, range: 50 },
      };
    case Mask.Brush:
      return { ...common, parameters: { lines: [] } };
    case Mask.Flow:
      return { ...common, parameters: { lines: [], flow: 10 } };
    case Mask.AiSubject:
      return { ...common, parameters: withMaskUpdatedFlag({ maskDataBase64: null, grow: 0, feather: 0 }) };
    case Mask.AiForeground:
      return { ...common, parameters: withMaskUpdatedFlag({ maskDataBase64: null, grow: 0, feather: 0 }) };
    case Mask.AiSky:
      return { ...common, parameters: withMaskUpdatedFlag({ maskDataBase64: null, grow: 0, feather: 0 }) };
    case Mask.AiDepth:
      return {
        ...common,
        parameters: withMaskUpdatedFlag({
          maskDataBase64: null,
          minDepth: 20,
          maxDepth: 100,
          minFade: 15,
          maxFade: 15,
          feather: 10,
        }),
      };
    case Mask.QuickEraser:
      return { ...common, parameters: withMaskUpdatedFlag({ maskDataBase64: null, grow: 50, feather: 50 }) };
    default:
      return { ...common, parameters: {} };
  }
};
