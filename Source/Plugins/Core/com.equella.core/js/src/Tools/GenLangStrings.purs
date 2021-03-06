module Tools.GenLangStrings where

import Prelude

import Common.CommonStrings (commonString)
import Control.Plus (empty)
import Data.Argonaut (encodeJson, stringify)
import Data.List (List, fromFoldable, singleton)
import Data.Symbol (class IsSymbol, SProxy(..), reflectSymbol)
import Data.Tuple (Tuple(..))
import Effect (Effect)
import Effect.Class.Console (log)
import Foreign.Object as SM
import OEQ.MainUI.SearchPage (coreStrings, rawStrings) as SearchPage
import OEQ.MainUI.SettingsPage (coreStrings, rawStrings) as SettingsPage
import OEQ.MainUI.Template (rawStrings, coreStrings) as Template
import OEQ.UI.ItemSummary.ItemComments as ItemComments
import OEQ.UI.Security.ACLEditor (aclRawStrings)
import OEQ.UI.Security.TermSelection (termRawStrings)
import OEQ.UI.Settings.UISettings (rawStrings) as UISettings
import Prim.Row (class Cons)
import Record (get)
import Type.Row (class RowToList, Cons, Nil, RLProxy(..))

foreign import data DynamicString :: Type

class ConvertToStrings a where
  genStrings :: String -> a -> List (Tuple String String)

foreign import courseString :: DynamicString
foreign import courseEditString :: DynamicString
foreign import entityStrings :: DynamicString
foreign import uiThemeSettingStrings :: DynamicString
foreign import genStringsDynamic :: (String -> String -> Tuple String String) -> String -> DynamicString -> Array (Tuple String String)

instance nilStrings :: ConvertToStrings (Tuple (RLProxy Nil) (Record r)) where
  genStrings _ _ = empty

instance consStrings :: (IsSymbol hs, Cons hs ht r' r,
    ConvertToStrings ht,
    ConvertToStrings (Tuple (RLProxy t) (Record r)))
  => ConvertToStrings (Tuple (RLProxy (Cons hs ht t)) (Record r)) where
  genStrings pfx (Tuple _ r) =
    genStrings (pfx <> ".") (Tuple (reflectSymbol (SProxy :: SProxy hs)) (get (SProxy :: SProxy hs) r))
    <> (genStrings pfx (Tuple (RLProxy :: RLProxy t) r))

instance record :: (RowToList a out, ConvertToStrings (Tuple (RLProxy out) (Record a)))
  => ConvertToStrings (Record a) where
  genStrings pfx r = genStrings pfx (Tuple (RLProxy :: RLProxy out) r)

instance stringString :: ConvertToStrings String where
  genStrings pfx a = singleton $ Tuple pfx a

instance prefixed :: ConvertToStrings a => ConvertToStrings (Tuple String a) where
  genStrings pfx (Tuple prefix r) = genStrings (pfx<>prefix) r

instance dynamic :: ConvertToStrings DynamicString where 
  genStrings pfx d = fromFoldable $ genStringsDynamic Tuple pfx d

genTopLevel :: forall r. ConvertToStrings r => {prefix::String, strings:: r} -> List (Tuple String String)
genTopLevel {prefix,strings} = genStrings "" (Tuple prefix strings)

main :: Effect Unit
main = do
  log $ stringify $ encodeJson $ SM.fromFoldable $
    genTopLevel Template.rawStrings <>
    genTopLevel Template.coreStrings <>
    genTopLevel SearchPage.rawStrings <>
    genTopLevel SearchPage.coreStrings <> 
    genTopLevel UISettings.rawStrings <>
    genTopLevel SettingsPage.rawStrings <>
    genTopLevel SettingsPage.coreStrings <>
    genTopLevel {prefix:"common", strings:commonString} <> 
    genTopLevel {prefix:"courses", strings:courseString} <> 
    genTopLevel {prefix:"courseedit", strings:courseEditString} <>
    genTopLevel {prefix:"entity", strings:entityStrings} <>
    genTopLevel {prefix:"newuisettings", strings: uiThemeSettingStrings} <>
    genTopLevel aclRawStrings <>
    genTopLevel termRawStrings <>
    genTopLevel ItemComments.coreStrings
