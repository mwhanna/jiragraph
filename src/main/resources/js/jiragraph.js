var gsylviedavies;
var $ = jQuery;
if (!gsylviedavies) {

    var commitRegex = /reverts commit ([A-Fa-f0-9]{40})/g;
    var svgNS = "http://www.w3.org/2000/svg";
    var xlinkNS = "http://www.w3.org/1999/xlink";
    var svgHolder = Object.create(null);
    svgHolder.commitsList = [];
    svgHolder.reverts = Object.create(null);

    function getOffset(el) {
        el = el && el.getBoundingClientRect();
        var scrollX = Number(window.scrollX || window.pageXOffset || 0);
        var scrollY = Number(window.scrollY || window.pageYOffset || 0);
        return el && {
                left: (el.left + scrollX),
                top: (el.top + scrollY)
            };
    }

    function redrawSvg() {
        var svg = document.createElementNS(svgNS, "svg");
        var now = Math.floor((new Date).getTime() / 1000);
        svg.setAttribute("now", now);
        svg.setAttribute('xmlns:xlink', 'http://www.w3.org/1999/xlink');
        svg.setAttribute("width", 65);
        svg.setAttribute("height", 80);
        svg.setAttribute("text-rendering", "optimizeLegibility");
        svg.setAttributeNS("http://www.w3.org/1999/xhtml", "style", "border: 0px; margin: 0px; padding: 0;");
        svg.id = "bit-booster";
        svgHolder.svg = svg;

        function makeStop(percent, opacity) {
            var stop = document.createElementNS(svgNS, "stop");
            stop.setAttribute("offset", percent);
            stop.setAttribute("stop-color", "#ffffff");
            stop.setAttribute("stop-opacity", opacity);
            return stop;
        }

        var defs = document.createElementNS(svgNS, "defs");
        var lg = document.createElementNS(svgNS, "linearGradient");
        lg.id = "grad1";
        lg.setAttribute("x1", "0");
        lg.setAttribute("y1", "0");
        lg.setAttribute("x2", "0");
        lg.setAttribute("y2", "1");
        lg.appendChild(makeStop("0%", "0.0"));
        lg.appendChild(makeStop("32%", "1.0"));
        lg.appendChild(makeStop("68%", "1.0"));
        lg.appendChild(makeStop("100%", "0.0"));
        defs.appendChild(lg);
        svg.appendChild(defs);

        var tbl = document.getElementById("bit-booster-tbl");
        if (!tbl) {
            var div = document.getElementById("viewissue-devstatus-panel");
            var nl = div.childNodes;
            for (var i = 0; i < nl.length; i++) {
                if (nl.item(i).className === 'mod-content') {
                    div = nl.item(i);
                    break;
                }
            }

            var head = document.getElementById("viewissue-devstatus-panel_heading");
            var hl = head.childNodes;
            for (var r = 0; r < hl.length; r++) {
                if (hl.item(r).className === 'ops') {
                    head = hl.item(r);
                    break;
                }
            }

            var arrayOfRepos = ["repo1", "repo2", "repo3"];
            /*
            Ajax call to Servlet, returns array
             */
            head.className = "aui-toolbar toolbar-group pluggable-ops";
            head.id = "repo-toolbar";
            for(var o = 0; o < arrayOfRepos.length; o++) {
                var temp = document.createElement("li");
                temp.className = "toolbar-item";
                var repoName = arrayOfRepos.get(o);
                var tempText = document.createTextNode(repoName);
                temp.appendChild(tempText);
                document.getElementById("repo-toolbar").appendChild(temp);
            }

            tbl = document.createElement('table');
            tbl.id = "bit-booster-tbl";
            tbl.style.width = "100%";
            var tr = tbl.insertRow();
            var tdL = tr.insertCell();
            var tdR = tr.insertCell();
            tdL.style.verticalAlign = "top";
            tdR.style.width = "99%";
            tdR.style.verticalAlign = "top";
            div.insertBefore(tbl, div.firstChild);
            tdL.appendChild(svg);
            svgHolder.tdL = tdL;
        } else {
            if (!svgHolder.tdL) {
                svgHolder.tdL = tbl.getElementsByTagName("td").item(0);
            }
            svgHolder.tdL.appendChild(svg);
        }
    }

    function f() {

        // http://stackoverflow.com/questions/9229645/remove-duplicates-from-javascript-array/9229821
        function uniq(a) {
            return a.sort().filter(function (item, pos, ary) {
                return !pos || item != ary[pos - 1];
            })
        }

        var COLORS = [
            "#034f84", "#79c753", "#f7786b", "#fae03c", "#98ddde", "#9896a4", "#dc4132", "#b08e6a", "#91a8d0", "#f7cac9"
        ];
        var dateOptionsLong = {
            year: 'numeric', month: 'short', day: 'numeric',
            hour: 'numeric', minute: 'numeric', second: 'numeric'
        };
        var dateOptionsShort = {
            year: 'numeric', month: 'short', day: 'numeric'
        };
        var laneWidth = 15;
        var laneLength = 35;
        var maxCol = 0;
        var elbows = [];
        var commitsTable = Object.create(null);
        var farLeftPosition = 9;

        var doIt = function () {
            elbows = [];
        }

        function storeElbow(x, y, commit) {
            if (!elbows[x]) {
                elbows[x] = [];
            }
            elbows[x][y] = commit;
        }

        function readElbow(x, y) {
            return elbows[x] && elbows[x][y];
        }

        function isMerge(my) {
            return my.parents && my.parents.length > 1;
        }

        function addGraphFunctions(my) {
            var commitsList = svgHolder.commitsList;
            var me = my;
            my.pathsDrawn = Object.create(null);

            if (my.drawPathTo) {
                return;
            }

            my.drawPathTo = function (commit) {
                if (my.pathsDrawn[commit.sha1]) {
                    return;
                }
                my.pathsDrawn[commit.sha1] = true;

                var offset = commit.col - my.col;
                var targetCol = my.col;
                if (offset > 0) {
                    targetCol += offset;
                }

                var distance = commit.row - my.row;

                // Collision avoidance:
                var hasCollision = false;
                var collisionFree = false;
                while (!collisionFree) {
                    var foundCollision = false;
                    for (var j = 1; j < distance; j++) {
                        var c = commitsList[my.row + j];
                        if (targetCol === c.col) {
                            hasCollision = true;
                            foundCollision = true;
                            targetCol++;
                            break;
                        }
                        var elbow = readElbow(my.row + j, targetCol);
                        if (elbow && elbow !== commit) {
                            hasCollision = true;
                            foundCollision = true;
                            targetCol++;
                            break;
                        }
                    }
                    if (!foundCollision) {
                        collisionFree = true;
                    }
                }

                var pos = my.pos(commit);
                if (hasCollision) {
                    maxCol = Math.max(maxCol, targetCol);

                    // Two ways to avoid collision:
                    // 1.) curve-around
                    // 2.) move myself over!
                    elbow = readElbow(my.row, targetCol);
                    if (isMerge(me) || (elbow && elbow !== me)) {
                        pos.setColor(targetCol);
                        my.curveRight(pos, targetCol - my.col, commit);
                    } else {
                        my.col = targetCol;
                        my.x = farLeftPosition + (laneWidth * my.col);
                        offset = commit.col - my.col;
                        pos = my.pos(commit);
                    }
                    pos.setColor(targetCol);

                    my.path(pos, commit.row - 1, targetCol, commit);
                    my.curveLeft(pos, commit.col - targetCol, commit);
                    commit.colorOverride = targetCol;
                } else {
                    if (offset > 0) {
                        my.curveRight(pos, offset, commit);
                        my.path(pos, commit.row, targetCol, commit);
                    } else if (offset < 0) {
                        my.path(pos, commit.row - 1, targetCol, commit);
                        my.curveLeft(pos, offset, commit);
                    } else {
                        my.path(pos, commit.row, targetCol, commit);
                    }
                }
            }

            my.plumb = function () {
                var commitsList = svgHolder.commitsList;
                if (my.isPlumbed) {
                    return;
                }
                var result = undefined;
                if (my.parents && my.parents.length > 0) {
                    for (var i = 0; i < my.parents.length; i++) {
                        var parent = commitsTable[my.parents[i]];
                        if (parent && !parent.isPlumbed) {

                            if (i == 0) {
                                result = parent.plumb();
                            } else {
                                parent.plumb();
                            }

                            var offset = parent.col - my.col;
                            var distance = parent.row - my.row;

                            if (offset === 0) {
                                offset = i;
                            }
                            if (offset >= 0) {
                                var col = my.col + offset;
                            } else {
                                col = my.col;
                            }


                            for (var j = 1; j < distance; j++) {
                                var c = commitsList[my.row + j];
                                if (c && !c.isPlumbed) {
                                    c.col = col + 1;
                                    c.x = farLeftPosition + (laneWidth * c.col);
                                    maxCol = Math.max(c.col, maxCol);
                                }
                            }
                        } else {
                            if (i == 0) {
                                result = me;
                            }
                        }
                    }
                } else {
                    result = me;
                }
                my.isPlumbed = true;
                return result;
            }

            my.draw = function () {
                if (my.isDone) {
                    return;
                }
                my.isDone = true;
                for (var i = 0; my.parents && i < my.parents.length; i++) {
                    var parent = commitsTable[my.parents[i]];
                    if (parent) {
                        my.drawPathTo(parent);
                        parent.draw();
                    } else {
                        // Merge-out..
                        my.path(my.pos(), my.row + 1, my.col, undefined, true);
                    }
                }
            }

            my.pos = function (targetCommit) {
                var commitsList = svgHolder.commitsList;
                var v = [my.x, my.y];
                v.setColor = function (col) {
                    if (Number(col) === col) {
                        v.color = COLORS[col % COLORS.length];
                        v.srcColor = v.color;
                    }
                }
                v.setColor(targetCommit && targetCommit.col);
                v.srcColor = COLORS[my.col % COLORS.length];
                if (!v.color) {
                    v.color = v.srcColor;
                }
                if (my.colorOverride) {
                    if (my.col !== 0) {
                        v.setColor(my.colorOverride);
                    }
                }

                v.below = function (targetRow) {
                    var c = commitsList[targetRow];
                    var y = c && c.y;
                    if (!y) {
                        y = commitsList[commitsList.length - 1].y + laneLength;
                    }
                    return [v[0], y];
                }
                v.right = function (amount) {
                    w = [v[0] + (laneWidth * amount), v[1] + laneLength];      // destination
                    if (targetCommit && commitsList.length > my.row + 1) {
                        w[1] = commitsList[my.row + 1].y;
                    }
                    return [
                        v[0] - 1, v[1] + (laneLength * 0.75),                         // bezier point 1
                        v[0] + (laneWidth * amount) + 1, v[1] + (laneLength * 0.25),  // bezier point 2
                        w[0], w[1]
                    ];
                }
                v.left = function (amount) {
                    return [
                        v[0] + 1, v[1] + (laneLength * 0.75),                         // bezier point 1
                        v[0] + (laneWidth * amount) - 1, v[1] + (laneLength * 0.25),  // bezier point 2
                        v[0] + (laneWidth * amount), targetCommit.y
                    ];
                }
                return v;
            }

            my.curveRight = function (pos, distanceAcross, targetCommit) {
                if (targetCommit) {
                    storeElbow(my.row + 1, my.col + distanceAcross, targetCommit);
                }
                var endPos = pos.right(distanceAcross);
                var path = document.createElementNS(svgNS, "path");
                path.setAttribute("d", "M" + pos.join(",") + "C" + endPos.join(","));
                path.setAttribute("stroke-width", 2);
                path.setAttribute("stroke-opacity", 1);
                path.setAttribute("opacity", 1);
                path.setAttribute("fill", "none");
                path.setAttribute("stroke", pos.color);
                my.drawEarlier(path);
                pos[0] = endPos[endPos.length - 2];
                pos[1] = endPos[endPos.length - 1];
            }

            my.curveLeft = function (pos, distanceAcross) {
                var endPos = pos.left(distanceAcross);
                var path = document.createElementNS(svgNS, "path");
                path.setAttribute("d", "M" + pos.join(",") + "C" + endPos.join(","));
                path.setAttribute("stroke-width", 2);
                path.setAttribute("stroke-opacity", 1);
                path.setAttribute("opacity", 1);
                path.setAttribute("fill", "none");
                path.setAttribute("stroke", pos.srcColor);
                my.drawEarlier(path);
                pos[0] = endPos[endPos.length - 2];
                pos[1] = endPos[endPos.length - 1];
            }

            my.path = function (pos, targetRow, targetCol, targetCommit, dashed) {
                var svg = svgHolder.svg;
                var commitsList = svgHolder.commitsList;

                // Every moment of collision-avoidance must be marked as an "elbow":
                if (!dashed && targetCommit) {
                    for (var i = my.row + 1; i < targetRow; i++) {
                        storeElbow(i, targetCol, targetCommit);
                    }
                }

                var path = document.createElementNS(svgNS, "path");
                var endPos = pos.below(targetRow);

                if (dashed && my.row !== commitsList.length - 1) {
                    // dashed should trail off after 70% of distance..
                    endPos[0] = pos[0] + laneWidth * 1.5;
                    endPos[1] = pos[1] + 0.7 * (endPos[1] - pos[1]);
                }

                path.setAttribute("d", "M" + pos.join(",") + "L" + endPos.join(","));
                path.setAttribute("stroke-width", 2);
                path.setAttribute("stroke-opacity", 1);
                if (dashed) {
                    path.setAttribute("stroke-dasharray", "15,3,3,3,3,3,3,3,3,3,3");
                }
                path.setAttribute("opacity", 1);
                if (targetCommit && targetCommit.col && my.col < targetCommit.col) {
                    path.setAttribute("stroke", pos.color);
                } else {
                    path.setAttribute("stroke", pos.srcColor);
                }
                svg.appendChild(path);
                pos[1] = endPos[1];

                var width = svg.getAttribute("width");
                if (width < pos[0]) {
                    svg.setAttribute("width", pos[0] + 10);
                }
            }

            my.drawEarlier = function (element) {
                var svg = svgHolder.svg;
                if (svg.firstChild) {
                    svg.insertBefore(element, svg.firstChild);
                } else {
                    svg.appendChild(element);
                }
            }

            my.circle = function () {
                var url = window.location.href;
                var x = url.lastIndexOf("/plugins/servlet/bb_net/");
                var y = url.lastIndexOf("/projects/");
                var target = "";
                if (x >= 0 && y >= 0) {
                    target = url.substr(0, x) + url.substr(y);
                    target += (target.indexOf('?') > y) ? '&' : '?';
                }

                var pos = my.pos();
                var svg = svgHolder.svg;
                var width = svg.getAttribute("width");
                var height = svg.getAttribute("height");

                var rect = document.createElementNS(svgNS, "rect");
                rect.id = "R_" + my.sha1;
                rect.setAttribute("x", 0);
                rect.setAttribute("y", Number(pos[1] - 14));
                rect.setAttribute("width", "100%");
                rect.setAttribute("height", 28);
                rect.setAttribute("stroke", "none");
                rect.setAttribute("stroke-width", 0);
                rect.setAttribute("fill", "transparent");
                svg.appendChild(rect);

                var circle = document.createElementNS(svgNS, "circle");
                circle.id = "C_" + my.sha1;
                circle.setAttribute("cx", pos[0]);
                circle.setAttribute("cy", pos[1]);
                circle.setAttribute("r", 4);
                circle.setAttribute("fill", !my.revert ? pos.color : (my.revert === 1 ? "red" : "orange"));
                circle.setAttribute("stroke", "none");
                svg.appendChild(circle);

                jqueryEnterAndLeave(rect);
                jqueryEnterAndLeave(circle);

                if (my.revert) {
                    var c1 = [pos[0] - 6, pos[1] - 6];
                    var c2 = [pos[0] + 6, pos[1] - 6];
                    var c3 = [pos[0] - 6, pos[1] + 6];
                    var c4 = [pos[0] + 6, pos[1] + 6];
                    var xL = document.createElementNS(svgNS, "path");
                    xL.setAttribute("d", "M" + c1.join(",") + "L" + c4.join(","));
                    xL.setAttribute("stroke-width", 3);
                    xL.setAttribute("stroke-opacity", 1);
                    xL.setAttribute("opacity", 1);
                    xL.setAttribute("stroke", my.revert === 1 ? "red" : "orange");
                    svg.appendChild(xL);

                    var xR = document.createElementNS(svgNS, "path");
                    xR.setAttribute("d", "M" + c2.join(",") + "L" + c3.join(","));
                    xR.setAttribute("stroke-width", 3);
                    xR.setAttribute("stroke-opacity", 1);
                    xR.setAttribute("opacity", 1);
                    xR.setAttribute("stroke", my.revert === 1 ? "red" : "orange");
                    svg.appendChild(xR);
                }

                if (true || window.location.pathname.indexOf("/bb_net/") >= 0) {
                    var hasTags = my.tags && my.tags.length > 0;
                    var hasBranches = my.branches && my.branches.length > 0;
                    var hasBoth = hasTags && hasBranches;
                    var insertBefore = undefined;
                    if (hasBranches) {
                        insertBefore = my.insertTag(pos, false, hasBoth, target);
                    }
                    if (hasTags) {
                        my.insertTag(pos, true, hasBoth, target, insertBefore);
                    }
                }

                if (width < pos[0]) {
                    svg.setAttribute("width", pos[0] + 10);
                }
                if (height < pos[1]) {
                    svg.setAttribute("height", pos[1] + 10);
                }
            }

            function jqueryEnterAndLeave(svgObj) {
                $(svgObj).mouseenter(function () {
                    sha = this.id.substring(2);
                    $("#T_" + sha).addClass("commitHover");
                }).mouseleave(function () {
                    sha = this.id.substring(2);
                    $("#T_" + sha).removeClass("commitHover");
                });
            }

            function truncateBranch(branch) {
                if (branch.length > 27) {
                    return branch.substr(0, 24) + "...";
                } else {
                    return branch;
                }
            }

            my.insertTag = function (pos, isTag, hasBoth, target, insertBefore) {
                var svg = svgHolder.svg;
                var posCopy = pos;
                pos = [posCopy[0], posCopy[1]];
                if (hasBoth) {
                    pos[1] += isTag ? -7 : 7;
                }
                var width = svg.getAttribute("width");
                var text = document.createElementNS(svgNS, "text");
                var objs = isTag ? my.tags : my.branches;

                // Copy "objs" and remove all "HEAD" refs from it.
                objs = objs.slice();
                for (var i = objs.length - 1; i >= 0; i--) {
                    var o = objs[i];
                    if (o.indexOf('HEAD ->') === 0) {
                        o = o.substr(7).trim();
                        objs[i] = o;
                    }
                    if (o === 'HEAD' || o.indexOf('HEAD ') === 0) {
                        objs.splice(i, 1);
                    }
                }

                text.setAttribute("x", pos[0] + 7);
                text.setAttribute("y", pos[1] + 3);
                text.setAttribute("font-size", "12px");
                text.textContent = truncateBranch(objs[0]);
                var links = [];
                links.push(document.createElementNS(svgNS, "a"));
                links[0].setAttributeNS(xlinkNS, "href", target + "until=" + objs[0]);
                links[0].setAttributeNS(xlinkNS, "title", objs[0]);
                links[0].appendChild(text);
                svg.appendChild(links[0]);

                if (isTag && !my.tagBox1) {
                    var bbox = links[0].getBBox();
                    my.tagBox1 = {
                        width: bbox.width,
                        height: bbox.height
                    };
                }
                if (!isTag && !my.brBox1) {
                    bbox = links[0].getBBox();
                    my.brBox1 = {
                        width: bbox.width,
                        height: bbox.height
                    };
                }

                var box = {
                    width: isTag ? my.tagBox1.width : my.brBox1.width,
                    height: isTag ? my.tagBox1.height : my.brBox1.height
                };

                if (objs.length > 1) {
                    text = document.createElementNS(svgNS, "text");
                    text.setAttribute("x", pos[0] + 6 + box.width);
                    text.setAttribute("y", pos[1] + 2);
                    text.setAttribute("font-size", "12px");
                    text.textContent = ", ";
                    svg.appendChild(text);

                    text = document.createElementNS(svgNS, "text");
                    text.setAttribute("x", pos[0] + 12 + box.width);
                    text.setAttribute("y", pos[1] + 3);
                    text.setAttribute("font-size", "12px");
                    links.push(document.createElementNS(svgNS, "a"));
                    if (objs.length == 2) {
                        text.textContent = truncateBranch(objs[1]);
                        links[1].setAttributeNS(xlinkNS, "href", target + "until=" + objs[1]);
                        links[1].setAttributeNS(xlinkNS, "title", objs[1]);
                    } else {
                        text.textContent = '[' + (objs.length - 1) + " more " + (isTag ? "tags" : "branches") + "]";
                        links[1].setAttribute("class", "noUnderline");
                        links[1].setAttributeNS(xlinkNS, "title", objs.slice(1).join(", "));
                    }
                    links[1].appendChild(text);
                    svg.appendChild(links[1]);

                    if (isTag && !my.tagBox2) {
                        bbox = links[1].getBBox();
                        my.tagBox2 = {
                            width: bbox.width,
                            height: bbox.height
                        };
                    }
                    if (!isTag && !my.brBox2) {
                        bbox = links[1].getBBox();
                        my.brBox2 = {
                            width: bbox.width,
                            height: bbox.height
                        };
                    }

                    var box2 = {
                        width: isTag ? my.tagBox2.width : my.brBox2.width,
                        height: isTag ? my.tagBox2.height : my.brBox2.height
                    };
                    box.width = box.width + box2.width + 4;
                    box.height = box2.height;
                }

                if (!hasBoth || (hasBoth && isTag)) {
                    rect = document.createElementNS(svgNS, "rect");
                    var boxWidth = box.width;
                    if (insertBefore) {
                        boxWidth = Math.max(boxWidth, insertBefore.boxWidth);
                    }
                    rect.setAttribute("x", pos[0] + 6);
                    rect.setAttribute("y", pos[1] - 16);
                    rect.setAttribute("rx", hasBoth ? "15" : "20");
                    rect.setAttribute("ry", hasBoth ? "15" : "20");
                    rect.setAttribute("width", boxWidth + 24);
                    rect.setAttribute("height", box.height + 17 + (hasBoth ? 15 : 0));
                    rect.setAttribute("stroke", "none");
                    rect.setAttribute("fill", "url(#grad1)");
                    rect.setAttribute("opacity", "1.0");
                    svg.insertBefore(rect, (insertBefore && insertBefore.domNode) || links[0]);
                }

                var icon = document.createElementNS(svgNS, "text");
                icon.setAttribute("font-family", "Atlassian Icons");
                icon.setAttribute("class", "icon");
                icon.setAttribute("x", pos[0] + box.width + 7 + (isTag ? 1 : 0));
                icon.setAttribute("y", pos[1] + 4);
                icon.textContent = isTag ? "\uf13b" : "\uf128";
                svg.appendChild(icon);

                if (width < pos[0] + 25 + box.width) {
                    svg.setAttribute("width", pos[0] + box.width + 30);
                    width = pos[0] + box.width + 30;
                }
                return {domNode: links[0], boxWidth: box.width};
            }
        }

        function parseDecorations(decs) {
            var tags = [];
            var branches = [];
            var toks = decs.split(", ");
            for (var i = 0; i < toks.length; i++) {
                var tok = toks[i];
                if (tok.indexOf("tag: ") == 0) {
                    tags.push(tok.substr(5));
                } else {
                    if (tok.indexOf("refs/pull-requests/") >= 0) {
                        // do nothing
                    } else {
                        branches.push(tok);
                    }
                }
            }
            return [uniq(tags), uniq(branches)];
        }

        function g(data) {

            function extractIds(s) {
                function reverse(s) {
                    for (var i = s.length - 1, o = ''; i >= 0; o += s[i--]) {
                    }
                    return o;
                }

                var jira_matcher = /\d+-[A-Z]+(?!-?[a-zA-Z]{1,10})/g
                var r = reverse(s)
                var matches = r.match(jira_matcher)
                if (!matches) {
                    matches = []
                }
                for (var j = 0; j < matches.length; j++) {
                    var m = reverse(matches[j])
                    matches[j] = m.replace(/-0+/, '-') // trim leading zeroes:  ABC-0123 becomes ABC-123
                }

                // need to remove duplicates, since they will cause n^2 links to be created (n = dups).
                return uniq(matches);
            }

            var lines = JSON.parse(data);
            doMattStuff(lines);

            function clear() {
                svgHolder.commitsList.length = 0;
                for (var k in doIt.commitsTable) {
                    delete doIt.commitsTable[k];
                }
                for (k in svgHolder.reverts) {
                    delete svgHolder.reverts[k];
                }
            }

            clear();

            var jira = lines['jira'];
            lines = lines['lines'];
            var now = Math.floor((new Date).getTime() / 1000);
            for (var i = 0; i < lines.length; i++) {
                var line = lines[i];
                var sha1 = line[2];
                var hasParents = line.length > 3 && line[3] && ("" !== line[3].trim());
                var parents = hasParents ? line[3].trim().split(' ') : undefined;
                var commitsList = svgHolder.commitsList;

                var c = {
                    isDone: false,
                    isPlumbed: false,
                    sha1: sha1,
                    x: farLeftPosition,
                    y: 8 + (25 * i),
                    row: commitsList.length,
                    col: 0
                };

                commitsList.push(c);
                commitsTable[c.sha1] = c;
                if (hasParents) {
                    c.parents = parents;
                }
                if ("" !== line[4]) {
                    var tagsAndBranches = parseDecorations(line[4]);
                    c.tags = tagsAndBranches[0];
                    c.branches = tagsAndBranches[1];
                }

                /*
                 var row = document.getElementById("T_" + sha1);
                 if (c) {
                 $(row).mouseenter(function () {
                 var svg = document.getElementById("bit-booster");
                 var sha = this.getAttribute("data-commitid");
                 if (sha) {
                 var c = svg.getElementById("C_" + sha);
                 c.setAttribute("class", "commitHover");
                 }
                 }).mouseleave(function () {
                 var svg = document.getElementById("bit-booster");
                 var sha = this.getAttribute("data-commitid");
                 if (sha) {
                 var c = svg.getElementById("C_" + sha);
                 c.removeAttribute("class");
                 }
                 });
                 }
                 */

                var row = false;
                if (c && !c.timeSet) {
                    if (row && !row.graphed) {
                        var nl = row.getElementsByTagName("time");
                        var time = nl.item(0);
                        var unixTime = Number(line[0]);
                        var date = new Date(unixTime * 1000);
                        var dateShort = date.toLocaleDateString(undefined, dateOptionsShort);
                        var dateLong = date.toLocaleDateString(undefined, dateOptionsLong);
                        var timeString = date.toLocaleTimeString();
                        if (dateShort === dateLong) {
                            dateLong = dateShort + " " + timeString;
                        }

                        var dateRfc = date.toISOString();
                        if (now - unixTime < 60 * 60 * 24 * 7) {
                            dateShort = line[1];
                        }

                        time.setAttribute("title", dateLong);
                        time.setAttribute("datetime", dateRfc);
                        time.textContent = dateShort;

                        // jira integration:
                        if (jira && jira.substr(0, 4).toLocaleLowerCase() === 'http') {
                            nl = row.getElementsByClassName("message-subject");
                            var spanMsg = nl.item(0);
                            var msg = spanMsg.textContent;
                            var ids = extractIds(msg);

                            for (var j = 0; ids && ids.length && j < ids.length; j++) {
                                var id = ids[j];
                                msg = msg.replace(
                                    new RegExp(id, 'g'),
                                    "<a class='commits-issues-trigger' data-single-issue='true' data-issue-keys='" + id + "' href='" + jira + "/browse/" + id + "'>" + id + "</a>");
                            }

                            // "innterHTML" speedup hack:
                            var newSpan = spanMsg.cloneNode(false);
                            newSpan.innerHTML = msg;
                            spanMsg.parentNode.replaceChild(newSpan, spanMsg);
                        }

                        row.graphed = true;
                    }
                    c.timeSet = true;
                }
            }

            for (i = 0; i < commitsList.length; i++) {
                addGraphFunctions(commitsList[i]);
            }

            function isHead(c) {
                if (c.branches && c.branches.length > 0) {
                    for (var i = 0; i < c.branches.length; i++) {
                        var b = c.branches[i];
                        if (b === "HEAD" || b.indexOf("HEAD ") === 0) {
                            return true;
                        }
                    }
                }
                return false;
            }

            var head = undefined;
            for (i = 0; i < commitsList.length; i++) {
                c = commitsList[i];
                if (isHead(c)) {
                    head = c;
                    break;
                }
            }

            for (i = 0; i < (head ? head.row : commitsList.length); i++) {
                c = commitsList[i];
                c.col++;
                c.x = farLeftPosition + (laneWidth * c.col);
            }

            if (head) {
                var tail = head.plumb();
                if (tail) {
                    for (i = tail.row + 1; i < commitsList.length; i++) {
                        c = commitsList[i];
                        if (c.col === 0) {
                            c.col++;
                            c.x = farLeftPosition + (laneWidth * c.col);
                        }
                    }
                }
            }

            var revertsGuaranteed = window.location.pathname.indexOf('/bb_net/') < 0;
            for (i = 0; i < commitsList.length; i++) {
                c = commitsList[i];

                row = document.getElementById("T_" + c.sha1);
                if (row && row.className.indexOf("revert") < 0) {
                    var revertCommit = svgHolder.reverts[c.sha1];
                    var revert = revertCommit && (revertsGuaranteed || revertCommit.col === 0);
                    var revertMaybe = !revert && revertCommit;
                    if (revert) {
                        row.className += " revert";
                    } else if (revertMaybe) {
                        row.className += " revertMaybe";
                    }

                    nl = row.getElementsByTagName("td");
                    var td;
                    for (j = 0; j < nl.length; j++) {
                        td = nl.item(j);
                        if (td.className.indexOf("message") >= 0) {
                            break;
                        }
                    }
                    if (td) {
                        nl = td.getElementsByTagName("span");
                        for (j = 0; j < nl.length; j++) {
                            var span = nl.item(j);
                            if (span.className.indexOf("message-subject") >= 0) {
                                var b;
                                if (revertMaybe) {
                                    b = document.createElement("b");
                                    b.textContent = "Possibly Reverted: ";
                                    span.parentNode.insertBefore(b, span);
                                    c.revert = 2;
                                } else if (revert) {
                                    b = document.createElement("b");
                                    b.textContent = "Reverted: ";
                                    span.parentNode.insertBefore(b, span);
                                    c.revert = 1;
                                } else {
                                    var title = span.title;
                                    var m = commitRegex.exec(title);
                                    while (m) {
                                        if (m[1]) {
                                            svgHolder.reverts[m[1]] = c;
                                        }
                                        m = commitRegex.exec(title);
                                    }
                                }
                            }
                        }
                    }
                }

                commitsList[i].plumb();

            }
            for (i = commitsList.length - 1; i >= 0; i--) {
                commitsList[i].draw();
            }
            for (i = commitsList.length - 1; i >= 0; i--) {
                commitsList[i].circle();
            }
        }

        doIt.g = g;
        doIt.commitsTable = commitsTable;
        return doIt;
    }

    window.addEventListener("load", function load(event) {
            window.removeEventListener("load", load);

            var doIt = f(event);

            function firstCommitWithN() {
                return "HEAD?n=3";
            }

            function drawGraph() {
                doIt();
                var svg = document.getElementById("bit-booster");
                console.log("Found (and deleted:) " + svg);
                if (svg) {
                    svg.parentNode.removeChild(svg);
                }
                redrawSvg();
                svg = svgHolder.svg;

                if (this.responseText.indexOf("bit-booster plugin requires a license") >= 0) {
                    var expired = document.getElementById("bit-booster-expired");
                    if (!expired) {
                        var a = document.createElement("a");
                        var path = window.location.pathname;
                        var x = path.indexOf("/plugins/servlet/");
                        if (x >= 0) {
                            a.setAttribute("href", path.substring(0, x + "/plugins/servlet/".length) + "upm");
                        } else {
                            a.setAttribute("href", "../plugins/servlet/upm");
                        }
                        a.id = "bit-booster-expired";
                        a.innerHTML = "Bit-Booster<br/>Commit&nbsp;Graph<br/>License<br/>Expired!";
                        var parent = svg.parentNode;
                        parent.insertBefore(a, svg);
                    }
                } else {
                    doIt.g(this.responseText);
                }
            }

            function getData() {
                var commitToFetch = firstCommitWithN();
                if (commitToFetch) {
                    var url = window.location.pathname;
                    if (url.indexOf("/bb_net/") >= 0) {
                        url = url.replace("/bb_net/", "/bb_dag/") + "/" + commitToFetch + "&all=y";
                    } else {
                        url = "../plugins/servlet/bb_dag" + window.location.pathname + "/" + commitToFetch;
                    }
                    var oReq = new XMLHttpRequest();
                    oReq.addEventListener("load", drawGraph);
                    oReq.open("GET", url);
                    oReq.send();
                }
            }

            getData();


            var fff = JIRA.DevStatus.devStatusModule.devStatusData.retrieveAggregateData;
            var a1 = JIRA.DevStatus.devStatusModule;
            var a2 = JIRA.DevStatus.devStatusModule.devStatusData;
            a2.on("beforeRequest", function () {
                console.log("JIRAGRAPH WUZ HERE BEFOREREQUEST!");
                getData();
            });

            /*
             var devStatusData = new JIRA.DevStatus.DevStatusData(
             {issueId: undefined, issueKey: undefined}
             );
             devStatusData.on('requestSuccess', function () {
             console.log("JiraGraph WUZ here Request Success!!!!!");
             getData();
             });
             */

        },
        false
    );

    function doMattStuff(lines) {
        var head = document.getElementById("viewissue-devstatus-panel_heading");
        var hl = head.childNodes;
        for (var r = 0; r < hl.length; r++) {
            if (hl.item(r).className === 'ops') {
                head = hl.item(r);
                break;
            }
        }

        var arrayOfRepos = [];

        var currentRepoObj = lines['currentRepo'];
        var currentRepo = currentRepoObj['repo'];
        var currentProj = currentRepoObj['project'];

        var repos = lines['repos'];
        for (var i = 0; i < repos.length; i++) {
            var item = repos[i];
            var repo = item['repo'];
            var projName = item['project'];
            var testValue = projName + "/" + repo;
            if ($.inArray(repo, arrayOfRepos) > -1) {
                var index = arrayOfRepos.indexOf(repo);
                arrayOfRepos.splice(index, 1);
                arrayOfRepos.push(testValue);
            }
            else {
                arrayOfRepos.push(repo);
            }
        }
        var url = window.location.pathname;
        if (url.indexOf("/bb_net/") >= 0) {
            url = url.replace("/bb_net/", "/bb_dag/") + "/&bbProj=" + currentProj + "&bbRepo=" + currentRepo + "&all=y";
        } else {
            url = "../plugins/servlet/bb_dag" + window.location.pathname + "/&bbProj=" + currentProj + "&bbRepo=" + currentRepo;
        }
        var oReq = new XMLHttpRequest();
        //oReq.addEventListener("load", drawGraph);
        oReq.open("GET", url);
        oReq.send();

        head.className = "aui-toolbar toolbar-group pluggable-ops";
        head.id = "repo-toolbar";
        for(var o = 0; o < arrayOfRepos.length; o++) {
            var temp = document.createElement("li");
            temp.className = "graphbar toolbar-item";
            var repoName = arrayOfRepos.get(o);
            temp.id = repoName;
            if (repoName === currentRepo) {
                temp.className = "graphbar toolbar-item current";
            }
            var tempText = document.createTextNode(repoName);
            temp.appendChild(tempText);
            document.getElementById("repo-toolbar").appendChild(temp);
        }

        $( ".graphbar" ).click(function() {
            $( ".current" ).removeClass("current");
            $(this).addClass("current");
        });
    }

}
gsylviedavies = true;