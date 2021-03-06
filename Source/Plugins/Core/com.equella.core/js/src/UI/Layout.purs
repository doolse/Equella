module OEQ.UI.Layout where 


import Prelude hiding (div)

import MaterialUI.Enums (js)
import MaterialUI.Hidden (hidden)
import MaterialUI.Paper (paper)
import MaterialUI.Styles (withStyles)
import React (ReactElement, statelessComponent, unsafeCreateLeafElement)
import React.DOM (div, div')
import React.DOM.Props as DP

dualPane :: { left :: Array ReactElement, right :: Array ReactElement } -> ReactElement
dualPane = unsafeCreateLeafElement $ withStyles styles $ statelessComponent \{classes,left,right} ->
    div' [
      hidden {mdDown:true, implementation: js} [
        div [DP.className classes.layoutDiv] [
            paper {className: classes.results, elevation: 4} left,
            paper {className: classes.refinements, elevation: 4} right
        ]
      ], 
      hidden {lgUp:true, implementation: js} [ div [DP.className classes.mobile] left ]
    ]
  where 
  styles theme = {
    results: {
      flexBasis: "75%",
      display: "flex",
      flexDirection: "column",
      padding: 16
    },
    refinements: {
      flexBasis: "25%",
      marginLeft: 16, 
      display: "flex", 
      flexDirection: "column",
      padding: theme.spacing.unit * 2
    },
    layoutDiv: {
      padding: theme.spacing.unit * 2,
      display: "flex",
      justifyContent: "space-around"
    }, 
    mobile: {
      padding: theme.spacing.unit
    }
  }
