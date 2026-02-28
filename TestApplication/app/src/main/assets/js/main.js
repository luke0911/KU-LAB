// 테스트베드별 설정 값
const testbedConfigs = {
    coex: {
        x_y_axis_rotation : false,
        map_area: {
            left: '0px', top: '0px'
        },
        small_arrow: {
            width: '40px',
            left:'2px', top: '-22px',
            transformOrigin: '-3px 20px',
        },
        small_dot: {
            width: '10px',
            left: '-5px', top: '-6px',
            constant_top: {a : -1, b : 3413.01896, c : 0.65412},
            constant_left: {a : 1, b : 202.3702, c : 0.65550},
        },
        big_arrow: {
            width: '300px',
        },
        big_dot: {
            width: '300px',
            constant_top: {a : -1, b : 3181.01896, c : 0.65412},
            constant_left: {a : 1, b : -27.6298, c : 0.65550},
        },
        X: {
            width: '12px',
            left: '-8px', top: '-9px',
            constant_top: {a : -1, b : 2232, c : 1.5288},
            constant_left: {a : 1, b : -132.91, c : 1.52539},
        },
        box: {
            constant_top: {a : -1, b : 3424, c : 0.65412},
            constant_left: {a : 1, b : 213, c : 0.65389},
        }
    },
    hansando: {
        x_y_axis_rotation : true,
        map_area: {
            left: '-200px', top: '0px',
        },
        small_arrow: {
            width: '40px',
            left:'2px', top: '-22px',
            transformOrigin: '-3px 20px',
        },
        small_dot: {
            width: '10px',
            left: '-5px', top: '-6px',
            constant_top: {a : -1, b : 1516.80451586, c : 0.94945788},
            constant_left: {a : 1, b : 220.97858997, c : 0.94299045},
        },
        big_arrow: {
            width: '120px',
        },
        big_dot: {
            width: '120px',
            constant_top: {a : -1, b : 1488, c : 0.92613636},
            constant_left: {a : 1, b : 160, c : 0.92613636},
        },
        X: {
            width: '12px',
            left: '-8px', top: '-9px',
            constant_top: {a : -1, b : 1438, c : 1.055},
            constant_left: {a : 1, b : -210, c : 1.055},
        },
        box: {
            constant_top: {a : 1, b : 0, c : 1},
            constant_left: {a : 1, b : 0, c : 1},
        }
    },
    suwonstation: {
        x_y_axis_rotation : false,
        map_area: {
            left: '-200px', top: '0px',
        },
        small_arrow: {
            width: '40px',
            left:'2px', top: '-22px',
            transformOrigin: '-3px 20px',
        },
        small_dot: {
            width: '10px',
            left: '-5px', top: '-6px',
            constant_top: {a : 1, b : 296, c : 0.81},
            constant_left: {a : 1, b : -623, c : 0.81},
        },
        big_arrow: {
            width: '120px',
        },
        big_dot: {
            width: '120px',
            constant_top: {a : 0, b : 0, c : 0},
            constant_left: {a : 0, b : 0, c : 0},
        },
        X: {
            width: '12px',
            left: '-8px', top: '-9px',
            constant_top: {a : 1, b : -238.68, c : 1.2345679},
            constant_left: {a : 1, b : 502.9, c : 1.2345679},
        },
        box: {
            constant_top: {a : 1, b : 0, c : 1},
            constant_left: {a : 1, b : 0, c : 1},
        }
    },
    hanasquare: {
        x_y_axis_rotation : false,
        map_area: {
            left: '-200px', top: '0px',
        },
        small_arrow: {
            width: '40px',
            left:'2px', top: '-22px',
            transformOrigin: '-3px 20px',
        },
        small_dot: {
            width: '10px',
            left: '-5px', top: '-6px',
            constant_top: {a : 1, b : 296, c : 0.81},
            constant_left: {a : 1, b : -623, c : 0.81},
        },
        big_arrow: {
            width: '120px',
        },
        big_dot: {
            width: '120px',
            constant_top: {a : 0, b : 0, c : 0},
            constant_left: {a : 0, b : 0, c : 0},
        },
        X: {
            width: '12px',
            left: '-8px', top: '-9px',
            constant_top: {a : 1, b : -238.68, c : 1.2345679},
            constant_left: {a : 1, b : 502.9, c : 1.2345679},
        },
        box: {
            constant_top: {a : 1, b : 0, c : 1},
            constant_left: {a : 1, b : 0, c : 1},
        }
    },

};

var clicked_x = 0
var clicked_y = 0
var prev_x = 0
var prev_y = 0
var config = {}
var currentAngle = 0; // 현재 회전 각도를 추적하기 위한 변수
var markers = [];
window.edges = [];
var draw_line_mode = true;
var clicked_marker;
var prev_clicked_marker;

function applyStyles(elementId, styleConfig) {
    const element = document.getElementById(elementId);
    if (element) {
        Object.assign(element.style, styleConfig);
    }
}

// 테스트베드 이름, 층 정보를 세팅
// mode에는 "test", "setting", "history"를 넣을 수 있음.
// [mode 설명]
// "test" : 큰 점, 큰 화살표 표시 (실시간 테스트를 위한 모드. 점 하나만 표시)
// "setting" : X, 작은 화살표 표시 (맵 수집을 위한 모드. 시작 위치/방향 설정 모드)
// "history" : 작은 점, 작은 화살표 표시 (맵 수집을 위한 모드. 점 history들 표시)
window.setTestbed = function (testbedName="coex", floor="B1", mode="test") {
    config = testbedConfigs[testbedName];

    // mode에 따라 보여야될 요소들 정의
    const modeConfig = {
        test: { show: ['big_dot', 'big_arrow', 'box'] },
        setting: { show: ['X', 'small_arrow'] },
        marker: { show: ['X', 'small_dot', 'small_arrow'] },
        history: { show: ['small_dot', 'small_arrow'] }
    };

    // 스타일 적용
    Object.entries(testbedConfigs[testbedName]).forEach(([elementId, styleConfig]) => {
        applyStyles(elementId, styleConfig);
    });

    // 맵 이미지 변경
    document.getElementById('map_img').src = `./images/maps/${testbedName}/${testbedName}_${floor}F.png`;

    // 아이콘 표시 설정
    document.querySelectorAll('.icon').forEach(icon => {
        icon.style.display = modeConfig[mode].show.includes(icon.id) ? 'block' : 'none';
    });

    const dotContainer = document.getElementById("dotContainer");
    dotContainer.style.left = '-1000px';
    dotContainer.style.top = '-1000px';


    if (mode === "setting" || mode === "marker") {
        enableTouchEvent(mode);
    }
    else {
        disableTouchEvent()
    }
}

// 터치를 비활성화하는 함수 추가
function disableTouchEvent() {
    const map_img = document.getElementsByTagName("body")[0];

    // 기존에 등록된 이벤트 리스너를 제거
    map_img.replaceWith(map_img.cloneNode(true));

    console.log("Touch events disabled.");
}

// 클릭 또는 터치하고자 하는 곳에 X 이미지를 띄우고 싶다면 아래 함수 맨 처음에 호출
function enableTouchEvent(mode) {
    const isTouchDevice = (navigator.maxTouchPoints || 'ontouchstart' in document.documentElement);

    if (document.readyState === "loading") {  // If document is still loading, wait for it to complete
        document.addEventListener("DOMContentLoaded", function () {
            document.removeEventListener("DOMContentLoaded", arguments.callee, false);
            setupTouchEvent(isTouchDevice);
        }, false);
    } else {  // `DOMContentLoaded` has already fired
        setupTouchEvent(isTouchDevice, mode);
    }
}

function setupTouchEvent(is_touch_device, mode="setting") {
    function handleEvent(event) {
        let x, y;
        if (event.type === 'touchstart') {
            // 터치 이벤트의 경우
            const touch = event.touches[0]; // 첫 번째 터치 포인트
            x = touch.pageX - this.offsetLeft;
            y = touch.pageY - this.offsetTop;
        } else {
            // 마우스 이벤트의 경우
            x = event.pageX - this.offsetLeft;
            y = event.pageY - this.offsetTop;
        }
        const dotContainer = document.getElementById("dotContainer");
        Object.assign(dotContainer.style, {
            left: `${x - 6}px`,
            top: `${y - 8}px`
        });
        function calculateCoordinate(value, config) {
            console.log(Number(value.replace("px", "")))

            return (config.a * Number(value.replace("px", "")) + config.b) * config.c;
        }
        if (!config.x_y_axis_rotation) {
            clicked_x = calculateCoordinate(dotContainer.style.top, config.X.constant_top);
            clicked_y = calculateCoordinate(dotContainer.style.left, config.X.constant_left);
        } else {
            clicked_y = calculateCoordinate(dotContainer.style.top, config.X.constant_top);
            clicked_x = calculateCoordinate(dotContainer.style.left, config.X.constant_left);
        }

        console.log(clicked_x, clicked_y);

        // "marker" 모드일 때,
        if (mode === "marker") {
            // SVG 컨테이너 찾기
            const svgContainer = document.getElementById('svgContainer');

            // 클릭한 점이 존재해 있던 점인지 확인하기
            const existingMarker = markers.find(marker =>
                Math.abs(marker.x - clicked_x) < 10 && Math.abs(marker.y - clicked_y) < 10
            );

            clicked_marker = {x:clicked_x, y:clicked_y}

            if (existingMarker) {
                clicked_marker = existingMarker
                if (markers.length > 1 && draw_line_mode) {
                    // const secondLastMarker = markers[markers.length - 1];
                    // if (existingMarker === secondLastMarker) {
                    if (existingMarker === prev_clicked_marker) {
                        prev_clicked_marker = clicked_marker
                        return;
                    }
                    const newLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                    newLine.setAttribute('x1', prev_x);
                    newLine.setAttribute('y1', prev_y);
                    if (!config.x_y_axis_rotation) {
                        newLine.setAttribute('x2', parseFloat((existingMarker.y/config.X.constant_left.c-config.X.constant_left.b)*config.X.constant_left.a-2), 2);
                        newLine.setAttribute('y2', parseFloat((existingMarker.x/config.X.constant_top.c-config.X.constant_top.b)*config.X.constant_top.a-1), 2);
                    }
                    else {
                        newLine.setAttribute('x2', parseFloat((existingMarker.x/config.X.constant_left.c-config.X.constant_left.b)*config.X.constant_left.a-2), 2);
                        newLine.setAttribute('y2', parseFloat((existingMarker.y/config.X.constant_top.c-config.X.constant_top.b)*config.X.constant_top.a-1), 2);
                    }

                    newLine.setAttribute('stroke', 'black'); // 선의 색상
                    newLine.setAttribute('stroke-width', 2); // 선의 두께
                    svgContainer.appendChild(newLine);
                }
                else {
                    draw_line_mode = true;
                    prev_clicked_marker = existingMarker;
                }

                if (!config.x_y_axis_rotation) {
                    prev_x = parseFloat((existingMarker.y/config.X.constant_left.c-config.X.constant_left.b)*config.X.constant_left.a -2, 2)
                    prev_y = parseFloat((existingMarker.x/config.X.constant_top.c-config.X.constant_top.b)*config.X.constant_top.a -1, 2)
                }
                else {
                    prev_x = parseFloat((existingMarker.x/config.X.constant_left.c-config.X.constant_left.b)*config.X.constant_left.a -2, 2)
                    prev_y = parseFloat((existingMarker.y/config.X.constant_top.c-config.X.constant_top.b)*config.X.constant_top.a -1, 2)
                }


                const lastMarker = clicked_marker
                const secondLastMarker = prev_clicked_marker
                edges.push({
                    start: secondLastMarker,
                    end: lastMarker
                });

                prev_clicked_marker = clicked_marker
                return; // 기존 마커가 눌린 경우, 새로운 마커를 생성하지 않음
            }

            markers.push({x:clicked_x, y:clicked_y});
            clicked_marker = markers[markers.length - 1];


            const newMarker = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
            newMarker.setAttribute("class", "marker")
            newMarker.setAttribute('cx', x-8); // 클릭된 x 좌표
            newMarker.setAttribute('cy', y-9); // 클릭된 y 좌표
            newMarker.setAttribute('r', 10); // 마커의 반지름
            newMarker.setAttribute('fill', 'blue'); // 마커의 색상
            newMarker.setAttribute("style", "z-index:4")
            svgContainer.appendChild(newMarker);

            if (markers.length > 1 && draw_line_mode) {
                const lastMarker = clicked_marker
                const secondLastMarker = prev_clicked_marker
                edges.push({
                    start: secondLastMarker,
                    end: lastMarker
                });
                const newLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                newLine.setAttribute('x1', prev_x);
                newLine.setAttribute('y1', prev_y);
                newLine.setAttribute('x2', x-8);
                newLine.setAttribute('y2', y-9);
                newLine.setAttribute('stroke', 'black'); // 선의 색상
                newLine.setAttribute('stroke-width', 2); // 선의 두께
                svgContainer.appendChild(newLine);
            }
            else {
                draw_line_mode = true;
            }

            prev_x = x-8
            prev_y = y-9
            prev_clicked_marker = clicked_marker
        }
    }
    function handleEventContextMenu(event) {
        draw_line_mode = false;
        event.preventDefault();
        let x, y;
        // 마우스 이벤트의 경우
        x = event.pageX - this.offsetLeft;
        y = event.pageY - this.offsetTop;

        const dotContainer = document.getElementById("dotContainer");
        Object.assign(dotContainer.style, {
            left: `${x - 6}px`,
            top: `${y - 8}px`
        });
        function calculateCoordinate(value, config) {
            return (config.a * Number(value.replace("px", "")) + config.b) * config.c;
        }
        if (!config.x_y_axis_rotation) {
            clicked_x = calculateCoordinate(dotContainer.style.top, config.X.constant_top);
            clicked_y = calculateCoordinate(dotContainer.style.left, config.X.constant_left);
        } else {
            clicked_y = calculateCoordinate(dotContainer.style.top, config.X.constant_top);
            clicked_x = calculateCoordinate(dotContainer.style.left, config.X.constant_left);
        }

        // console.log(clicked_x, clicked_y);

        // "marker" 모드일 때,
        if (mode === "marker") {
            // 클릭한 점이 존재해 있던 점인지 확인하기
            const existingMarker = markers.find(marker => {
                // console.log(Math.abs(marker.x - clicked_x) , Math.abs(marker.y - clicked_y));
                return Math.abs(marker.x - clicked_x) < 10 && Math.abs(marker.y - clicked_y) < 10;
            });

            if (existingMarker) {
                const markerIndex = markers.findIndex(marker => marker.x === existingMarker.x && marker.y === existingMarker.y);
                const svgContainer = document.getElementById('svgContainer');

                // Find the actual DOM node corresponding to the existing marker
                const allMarkers = Array.from(svgContainer.getElementsByTagName('circle'));
                const markerElement = allMarkers.find(element => {
                    if (!config.x_y_axis_rotation) {
                        return Math.abs(parseFloat(element.getAttribute('cx')) - (existingMarker.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a + 8) < 10 &&
                        Math.abs(parseFloat(element.getAttribute('cy')) - (existingMarker.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a + 9) < 10
                    }
                    else {
                        return Math.abs(parseFloat(element.getAttribute('cx')) - (existingMarker.x / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a + 8) < 10 &&
                        Math.abs(parseFloat(element.getAttribute('cy')) - (existingMarker.y / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a + 9) < 10
                    }
                });

                if (markerElement) {
                    svgContainer.removeChild(markerElement);
                }

                // Remove related edges
                // const linesToRemove = edges.filter(edge => edge.start === existingMarker || edge.end === existingMarker);
                const linesToRemove = edges.filter(edge => arePointsEqual(edge.start, existingMarker) || arePointsEqual(edge.end, existingMarker));
                console.log(linesToRemove)
                linesToRemove.forEach(line => {
                    const lineElements = Array.from(svgContainer.getElementsByTagName('line'));
                    lineElements.forEach(lineElement => {
                        const x1 = parseFloat(lineElement.getAttribute('x1'));
                        const y1 = parseFloat(lineElement.getAttribute('y1'));
                        const x2 = parseFloat(lineElement.getAttribute('x2'));
                        const y2 = parseFloat(lineElement.getAttribute('y2'));

                        // const startX = parseFloat(Math.round(line.start.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                        // const startY = parseFloat(Math.round(line.start.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2);
                        // const endX = parseFloat(Math.round(line.end.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                        // const endY = parseFloat(Math.round(line.end.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a  - 1, 2);

                        var startX = 0
                        var startY = 0
                        var endX = 0
                        var endY = 0
                        if (!config.x_y_axis_rotation) {
                            startX = parseFloat((line.start.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                            startY = parseFloat((line.start.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2);
                            endX = parseFloat((line.end.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                            endY = parseFloat((line.end.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2);

                        }
                        else {
                            startX = parseFloat((line.start.x / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                            startY = parseFloat((line.start.y / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2);
                            endX = parseFloat((line.end.x / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2);
                            endY = parseFloat((line.end.y / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2);
                        }

                        console.log(`start : ${line.start.x}, ${line.start.y} / end : ${line.end.x}, ${line.end.y}`)
                        if ((arePointsEqual({x:x1, y:y1}, {x:startX, y:startY}) && arePointsEqual({x:x2, y:y2}, {x:endX, y:endY}))
                            || (arePointsEqual({x:x1, y:y1}, {x:endX, y:endY}) && arePointsEqual({x:x2, y:y2}, {x:startX, y:startY})))
                        {
                            svgContainer.removeChild(lineElement);
                        }
                    });
                });

                // Remove from markers and edges arrays
                markers.splice(markerIndex, 1);
                // edges = edges.filter(edge => edge.start !== existingMarker && edge.end !== existingMarker);
                edges = edges.filter(edge => !arePointsEqual(edge.start, existingMarker) && !arePointsEqual(edge.end, existingMarker));
            }
        }
    }
    const map_img = document.getElementsByTagName("body")[0];
    map_img.addEventListener(is_touch_device ? 'touchstart' : 'click', handleEvent);
    map_img.addEventListener('contextmenu', handleEventContextMenu);
}

// 점 또는 X 이미지에 화살표의 각도 설정
window.rotateArrow = function (targetAngle) {
    const arrow = document.querySelector('#small_arrow, #big_arrow').style.display === 'block' ?
        document.querySelector('#small_arrow') : document.querySelector('#big_arrow');

    // 현재 각도와 목표 각도 사이의 차이를 계산합니다.
    var angleDifference = targetAngle - currentAngle;
    // -180도와 +180도 사이의 범위로 각도 차이를 정규화합니다.
    angleDifference = (angleDifference + 180) % 360 - 180;

    // 최소 회전을 위해 각도 차이가 180보다 크면 360에서 각도 차이를 뺍니다.
    if (angleDifference > 180) {
        angleDifference -= 360;
    }
    // 최소 회전을 위해 각도 차이가 -180보다 작으면 360을 더합니다.
    else if (angleDifference < -180) {
        angleDifference += 360;
    }

    // 현재 각도에 각도 차이를 더하여 새로운 회전 각도를 계산합니다.
    currentAngle += angleDifference;

    // 화살표 이미지를 새로운 각도로 회전시킵니다.
    if (!config.x_y_axis_rotation)
        arrow.style.transform = `rotate(${currentAngle}deg)`;
    else
        arrow.style.transform = `rotate(${currentAngle - 90}deg)`;
}

// 터치한 곳의 좌표값을 가져오기 위한 함수 (안드로이드 전용)
window.getClickedPosition = function () {
    return clicked_x + "\t" + clicked_y
}

window.show_my_position_with_history = function (x_pos, y_pos) {
    const dot = document.getElementById("dotContainer");
    const {a: aTop, b: bTop, c: cTop} = config.small_dot.constant_top;
    const {a: aLeft, b: bLeft, c: cLeft} = config.small_dot.constant_left;
    if (!config.x_y_axis_rotation) {
        dot.style.top = `${(aTop * x_pos + bTop) * cTop}px`;
        dot.style.left = `${(aLeft * y_pos + bLeft) * cLeft}px`;
    }
    else {
        dot.style.top = `${(aTop * y_pos + bTop) * cTop}px`;
        dot.style.left = `${(aLeft * x_pos + bLeft) * cLeft}px`;
    }


    const smallDot = document.getElementById("small_dot").cloneNode(true); // small_dot을 복제
    smallDot.style.position = "absolute"; // 복제된 small_dot의 위치 설정을 절대 위치로 변경
    smallDot.style.zIndex = "6"
    smallDot.style.top = `${parseFloat(dot.style.top)+2}px`;
    smallDot.style.left = `${parseFloat(dot.style.left)+3}px`;

    document.body.appendChild(smallDot); // 복제된 small_dot을 body에 추가
}

window.show_my_position = function (x_pos, y_pos) {
    const dot = document.getElementById("dotContainer");
    if (dot.style.display === "none")
        dot.style.display = "block"
    const {a: aTop, b: bTop, c: cTop} = config.big_dot.constant_top;
    const {a: aLeft, b: bLeft, c: cLeft} = config.big_dot.constant_left;
    if (!config.x_y_axis_rotation) {
        dot.style.top = `${(aTop * x_pos + bTop) * cTop}px`;
        dot.style.left = `${(aLeft * y_pos + bLeft) * cLeft}px`;
    }
    else {
        dot.style.top = `${(aTop * y_pos + bTop) * cTop}px`;
        dot.style.left = `${(aLeft * x_pos + bLeft) * cLeft}px`;
    }
}

window.showArea = function (x_min, x_max, y_min, y_max) {
    const box = document.getElementById("box");
    const {a: aTop, b: bTop, c: cTop} = config.box.constant_top;
    const {a: aLeft, b: bLeft, c: cLeft} = config.box.constant_left;
    box.style.top = `${(aTop * x_max + bTop) * cTop}px`;
    box.style.left = `${(aLeft * y_min + bLeft) * cLeft}px`;
    box.style.height = `${(x_max - x_min)*cTop}px`
    box.style.width = `${(y_max - y_min)*cLeft}px`
    box.style.display = 'block';
}

window.removeArea = function (){
    const box = document.getElementById("box");
    box.style.display = 'none';
}

// 부동 소수점 비교를 위한 함수
function areFloatsEqual(a, b, epsilon = 0.0001) {
    return Math.abs(a - b) < epsilon;
}

// 두 점이 같은지 비교하는 함수
function arePointsEqual(point1, point2) {
    return areFloatsEqual(point1.x, point2.x) && areFloatsEqual(point1.y, point2.y);
}

window.exportJson = function() {
    console.log(markers)
    console.log(edges)
    const jsonOutput = {
        nodes: markers.map((marker, index) => ({
            id: `Node${index + 1}`,
            coords: [
                parseFloat(marker.x.toFixed(2)),
                parseFloat(marker.y.toFixed(2))
            ]
        })),
        edges: edges
            .filter(edge => {
                const startIndex = markers.findIndex(marker =>
                    arePointsEqual(marker, edge.start)
                );
                const endIndex = markers.findIndex(marker =>
                    arePointsEqual(marker, edge.end)
                );
                if (startIndex === -1 || endIndex === -1) return false;

                const distance = Math.sqrt(
                    Math.pow(edge.end.x - edge.start.x, 2) +
                    Math.pow(edge.end.y - edge.start.y, 2)
                );

                return distance > 0;
            })
            .map(edge => {
                const startIndex = markers.findIndex(marker =>
                    arePointsEqual(marker, edge.start)
                );
                const endIndex = markers.findIndex(marker =>
                    arePointsEqual(marker, edge.end)
                );

                const distance = Math.sqrt(
                    Math.pow(edge.end.x - edge.start.x, 2) +
                    Math.pow(edge.end.y - edge.start.y, 2)
                );
                const roundedDistance = parseFloat(distance.toFixed(2));

                return {
                    start: `Node${startIndex + 1}`,
                    end: `Node${endIndex + 1}`,
                    distance: roundedDistance
                };
            })
    };
    console.log(JSON.stringify(jsonOutput, null, 2));
};



// JSON 데이터를 인자로 받아 markers와 edges 배열에 데이터를 저장하는 함수
window.loadJson = function (jsonData) {
    // markers 배열 초기화 및 데이터 저장 (id 없이)
    markers = jsonData.nodes.map(node => ({
        x: config.x_y_axis_rotation? node.coords[1] : node.coords[0],
        y: config.x_y_axis_rotation? node.coords[0] : node.coords[1]
    }));
    console.log(markers)
    // edges 배열 초기화 및 데이터 저장 (distance 없이)
    edges = jsonData.edges.map(edge => {
        const startMarker = markers.find((marker, index) => `Node${index + 1}` === edge.start);
        const endMarker = markers.find((marker, index) => `Node${index + 1}` === edge.end);
        return {
            start: { x: startMarker.x, y: startMarker.y },
            end: { x: endMarker.x, y: endMarker.y }
        };
    });
    console.log(edges)


    // 마커와 엣지를 SVG에 추가
    renderMarkersAndEdges();
    draw_line_mode = false;
}

// 마커와 엣지를 SVG에 추가하는 함수
function renderMarkersAndEdges() {
    const svgContainer = document.getElementById('svgContainer');

    // 마커 추가
    markers.forEach(marker => {
        const newMarker = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        newMarker.setAttribute("class", "marker");
        newMarker.setAttribute('cx', parseFloat((marker.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2), 2);
        newMarker.setAttribute('cy', parseFloat((marker.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1), 2);
        newMarker.setAttribute('r', 10);
        newMarker.setAttribute('fill', 'blue');
        svgContainer.appendChild(newMarker);

    });

    // 엣지 추가
    edges.forEach(edge => {
        const newLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');
        newLine.setAttribute('x1', parseFloat((edge.start.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2));
        newLine.setAttribute('y1', parseFloat((edge.start.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2));
        newLine.setAttribute('x2', parseFloat((edge.end.y / config.X.constant_left.c - config.X.constant_left.b) * config.X.constant_left.a - 2, 2));
        newLine.setAttribute('y2', parseFloat((edge.end.x / config.X.constant_top.c - config.X.constant_top.b) * config.X.constant_top.a - 1, 2));
        newLine.setAttribute('stroke', 'black');
        newLine.setAttribute('stroke-width', 2);
        svgContainer.appendChild(newLine);
    });
}

// 새로운 함수: 좌표 배열을 받아 모든 점을 찍는 함수
window.plotMultiplePoints = function(coordinates) {
    if (!Array.isArray(coordinates)) {
        console.error("Input must be an array of coordinates");
        return;
    }

    coordinates.forEach(coord => {
        if (Array.isArray(coord) && coord.length === 2) {
            const [x, y] = coord;
            if (typeof x === 'number' && typeof y === 'number') {
                show_my_position_with_history(x, y);
            } else {
                console.warn(`Invalid coordinate: (${x}, ${y}). Skipping.`);
            }
        } else {
            console.warn(`Invalid coordinate format: ${coord}. Skipping.`);
        }
    });

    console.log(`Plotted ${coordinates.length} points.`);
}


window.allReset = function() {
    // small_dot의 원본 요소를 제외하고 복제된 모든 small_dot 요소 삭제
    document.querySelectorAll("#small_dot").forEach((dot, index) => {
        if (index !== 0) {  // 첫 번째 요소(원본)는 제외하고 삭제
            dot.remove();
        }
    });

    console.log("All duplicated small_dot elements have been removed.");
};



// 페이지 로드 시 자동으로 특정 테스트베드 설정 적용
window.onload = function () {
    // setTestbed('coex', "B1", "test"); // 기본적으로 적용할 테스트베드 이름을 여기에 입력
    // setTestbed('coex', "B1", "marker"); // 기본적으로 적용할 테스트베드 이름을 여기에 입력
    // setTestbed('coex', "B1", "marker"); // 기본적으로 적용할 테스트베드 이름을 여기에 입력
    // rotateArrow(0)
    setTestbed('hansando', "0", "setting"); // 기본적으로 적용할 테스트베드 이름을 여기에 입력
    // show_my_position_with_history(0, 0)
    // show_my_position_with_history(0, 4306)
    // show_my_position_with_history(2663.5, 4306)
    // show_my_position_with_history(1988.5, 6278)
    // show_my_position_with_history(1988.5, 6500)
    // show_my_position(0, 0)
    // show_my_position(0, 4306)
    // show_my_position(2663.5, 4306)
    // show_my_position(1988.5, 6278)
    // showArea(0, 5790, 0, 5790)
};